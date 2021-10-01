package root.tse.infrastructure.exchange_gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Hex;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBar;
import org.ta4j.core.BaseBarSeries;
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties;
import root.tse.domain.strategy_execution.ExchangeGateway;
import root.tse.domain.strategy_execution.Interval;
import root.tse.domain.strategy_execution.trade.Order;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.util.CollectionUtils.isEmpty;
import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED;
import static root.tse.domain.strategy_execution.trade.OrderStatus.NOT_FILLED;

@Slf4j
@RequiredArgsConstructor
public class CurrencyComExchangeGateway implements ExchangeGateway {

    private static final String SERIES = "series[{}, {}]";

    private static final int TIMESTAMP_INDEX = 0;
    private static final int OPEN_PRICE_INDEX = 1;
    private static final int HIGH_PRICE_INDEX = 2;
    private static final int LOW_PRICE_INDEX = 3;
    private static final int CLOSE_PRICE_INDEX = 4;
    private static final int VOLUME_INDEX = 5;

    private static final String REQUEST_PARAM_VALUE_FORMAT = "%s=%s";
    private static final String REQUEST_PARAMS_DELIMITER = "&";
    private static final Charset UTF_8 = StandardCharsets.UTF_8;
    private static final String SIGNATURE_HASHING_ALGORITHM = "HmacSHA256";

    private final ExchangeGatewayConfigurationProperties configurationProperties;
    private final RetryTemplate retryTemplate;
    private final RateLimiter rateLimiter;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public Optional<BarSeries> getSeries(String symbol, Interval interval, Integer seriesLength) {
        try {
            var seriesData = performSeriesDataRetrieval(symbol, interval, seriesLength);
            log.debug(">>> retrieved " + SERIES + ": {}", symbol, interval, seriesData);
            return createSeries(seriesData);
        } catch (Exception e) {
            log.error(">>> " + SERIES + " retrieval failed.", symbol, interval, e);
            return Optional.empty();
        }
    }

    @Override
    public Order execute(Order order) {
        try {
            var orderExecutionResult = performOrderExecution(order);
            log.debug(">>> order has been executed: {}", orderExecutionResult);
            return formExecutedOrder(order, orderExecutionResult);
        } catch (Exception e) {
            log.error(">>> exception occurred during execution of order {}", order, e);
            return order.toBuilder().status(NOT_FILLED).build();
        }
    }

    @SneakyThrows
    private List<List<Object>> performSeriesDataRetrieval(String symbol, Interval interval, Integer seriesLength) {
        var seriesApiUri = configurationProperties.getSeriesUri();
        var intervalRepresentation = configurationProperties.getIntervalRepresentation(interval);
        var uri = UriComponentsBuilder.fromHttpUrl(seriesApiUri)
            .queryParam("symbol", symbol)
            .queryParam("interval", intervalRepresentation)
            .queryParam("limit", seriesLength)
            .toUriString();
        var responseEntity = retryTemplate.execute(retryContext -> {
            rateLimiter.acquire();
            return restTemplate.exchange(uri, HttpMethod.GET, HttpEntity.EMPTY, String.class);
        });
        validate(responseEntity);
        var responseType = new TypeReference<List<List<Object>>>(){};
        return objectMapper.readValue(responseEntity.getBody(), responseType);
    }

    @SneakyThrows
    private OrderExecutionResult performOrderExecution(Order order) {
        var uri = configurationProperties.getOrderUri();
        var requestBody = buildOrderRequestBody(order);
        var headers = getHeadersForAuthorizedOperation();
        var requestEntity = new HttpEntity<>(requestBody, headers);
        var responseEntity = retryTemplate.execute(retryContext -> {
            rateLimiter.acquire();
            return restTemplate.exchange(uri, HttpMethod.POST, requestEntity, String.class);
        });
        validate(responseEntity);
        return objectMapper.readValue(responseEntity.getBody(), OrderExecutionResult.class);
    }

    private void validate(ResponseEntity<?> responseEntity) {
        var statusCode = responseEntity.getStatusCode();
        if (statusCode.isError()) {
            throw new RuntimeException(format(">>> request failed: %s", statusCode.toString()));
        }
        if (isNull(responseEntity.getBody())) {
            throw new RuntimeException(">>> response does not contain body");
        }
    }

    private Optional<BarSeries> createSeries(List<List<Object>> seriesData) {
        var bars = seriesData.stream()
            .<Bar>map(barData -> {
                var duration = Duration.ZERO;
                var barTime = Instant.ofEpochMilli(Long.parseLong(String.valueOf(barData.get(TIMESTAMP_INDEX))));
                var zonedBarTime = ZonedDateTime.ofInstant(barTime, ZoneId.systemDefault());
                var open = String.valueOf(barData.get(OPEN_PRICE_INDEX));
                var high = String.valueOf(barData.get(HIGH_PRICE_INDEX));
                var low = String.valueOf(barData.get(LOW_PRICE_INDEX));
                var close = String.valueOf(barData.get(CLOSE_PRICE_INDEX));
                var volume = String.valueOf(barData.get(VOLUME_INDEX));
                return new BaseBar(duration, zonedBarTime, open, high, low, close, volume);
            })
            .collect(toList());
        return Optional.of(new BaseBarSeries(bars));
    }

    private String buildOrderRequestBody(Order order) {
        var requestBody = Map.of(
            "timeInForce", "FOK",
            "recvWindow", "5000",
            "type", "MARKET",
            "symbol", order.getSymbol(),
            "quantity", order.getAmount().toString(),
            "timestamp", order.getTimestamp().toString(),
            "side", order.getType().name()
        ).entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .map(this::buildRequestParamValuePair)
            .collect(Collectors.joining(REQUEST_PARAMS_DELIMITER));
        return sign(requestBody);
    }

    private String buildRequestParamValuePair(Map.Entry<String, String> entry) {
        return buildRequestParamValuePair(entry.getKey(), entry.getValue());
    }

    @SneakyThrows
    private String buildRequestParamValuePair(String param, String value) {
        var encodedValue = URLEncoder.encode(value, UTF_8.toString());
        return format(REQUEST_PARAM_VALUE_FORMAT, param, encodedValue);
    }

    @SneakyThrows
    private String sign(String requestBody) {
        var sha256HMAC = Mac.getInstance(SIGNATURE_HASHING_ALGORITHM);
        sha256HMAC.init(new SecretKeySpec(configurationProperties.getSecretKey().getBytes(UTF_8), SIGNATURE_HASHING_ALGORITHM));
        var signatureValue = Hex.encodeHexString(sha256HMAC.doFinal(requestBody.getBytes(UTF_8)));
        var signatureParamValuePair = buildRequestParamValuePair("signature", signatureValue);
        return requestBody + REQUEST_PARAMS_DELIMITER + signatureParamValuePair;
    }

    private HttpHeaders getHeadersForAuthorizedOperation() {
        var headers = new HttpHeaders();
        headers.setContentType(APPLICATION_FORM_URLENCODED);
        headers.add("X-MBX-APIKEY", configurationProperties.getApiKey());
        return headers;
    }

    private Order formExecutedOrder(Order order, OrderExecutionResult orderExecutionResult) {
        validate(orderExecutionResult);
        var amount = getAmountAtOrderExecutionTime(order, orderExecutionResult);
        var price = getPriceAtOrderExecutionTime(order, orderExecutionResult);
        return order.toBuilder().status(FILLED).amount(amount).price(price).build();
    }

    private void validate(OrderExecutionResult result) {
        var status = result.getStatus();
        if (!"FILLED".equals(status)) {
            throw new RuntimeException(format(">>> order has been considered as FAILED due to status [%s].", status));
        }
        var fills = result.getFills();
        if (isEmpty(fills)) {
            throw new RuntimeException(">>> order result does not contain fills.");
        }
        if (fills.size() > 1) {
            log.warn(">>> order result contains more than 1 fill: {}", result);
        }
    }

    private Double getAmountAtOrderExecutionTime(Order order, OrderExecutionResult orderExecutionResult) {
        var originalAmount = order.getAmount();
        var amountAtOrderExecutionTime = Double.valueOf(orderExecutionResult.getExecutedQty());
        if (!originalAmount.equals(amountAtOrderExecutionTime)) {
            log.warn(">>> original amount [{}] is different from amount at order execution time [{}].",
                originalAmount, amountAtOrderExecutionTime);
        }
        return amountAtOrderExecutionTime;
    }

    private Double getPriceAtOrderExecutionTime(Order order, OrderExecutionResult orderExecutionResult) {
        var originalPrice = order.getPrice();
        var fill = orderExecutionResult.getFills().get(0);
        var priceAtOrderExecutionTime = Double.valueOf(fill.getPrice());
        if (!originalPrice.equals(priceAtOrderExecutionTime)) {
            log.warn(">>> original price [{}] is different from price at order execution time [{}].",
                originalPrice, priceAtOrderExecutionTime);
        }
        return priceAtOrderExecutionTime;
    }
}
