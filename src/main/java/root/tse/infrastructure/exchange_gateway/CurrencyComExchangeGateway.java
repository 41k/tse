package root.tse.infrastructure.exchange_gateway;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.RateLimiter;
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
import root.tse.domain.ExchangeGateway;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.order.OrderType;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED;
import static org.springframework.util.CollectionUtils.isEmpty;

@Slf4j
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

    private final Map<OrderExecutionType, Function<Order, Order>> orderExecutors = Map.of(
        OrderExecutionType.STUB, this::executeStubOrder,
        OrderExecutionType.MARKET, this::executeMarketOrder
    );

    private final ExchangeGatewayConfigurationProperties configurationProperties;
    private final CurrentPriceProviderFactory currentPriceProviderFactory;
    private final RetryTemplate retryTemplate;
    private final RateLimiter rateLimiter;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final Map<String, Map<OrderType, Double>> currentPrices = new ConcurrentHashMap<>();

    private CurrentPriceProvider currentPriceProvider;

    public CurrencyComExchangeGateway(
        ExchangeGatewayConfigurationProperties configurationProperties,
        CurrentPriceProviderFactory currentPriceProviderFactory,
        RetryTemplate retryTemplate,
        RateLimiter rateLimiter,
        RestTemplate restTemplate,
        ObjectMapper objectMapper,
        Clock clock
    ) {
        this.configurationProperties = configurationProperties;
        this.currentPriceProviderFactory = currentPriceProviderFactory;
        this.retryTemplate = retryTemplate;
        this.rateLimiter = rateLimiter;
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.clock = clock;
        startNewCurrentPriceProvider();
    }

    // Note: the last bar which is returned by the API is incomplete so it is skipped.
    // More details:
    // If call for e.g. bars with ONE_DAY interval is made at 2021-10-11 05:00
    // then API returns bars with the last bar which has open time 2021-10-11 00:00
    // and such bar has intermediate HIGH, LOW, CLOSE prices since final prices will
    // be assigned by the end of the interval, in such case at 2021-10-12 00:00.
    @Override
    public Optional<BarSeries> getSeries(String symbol, Interval interval, Integer seriesLength) {
        try {
            var seriesData = performSeriesDataRetrieval(symbol, interval, seriesLength);
            log.debug(">>> retrieved " + SERIES + ": {}", symbol, interval, seriesData);
            return createSeries(seriesData, interval);
        } catch (Exception e) {
            log.error(">>> " + SERIES + " retrieval failed.", symbol, interval, e);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Map<String, Map<OrderType, Double>>> getCurrentPrices(List<String> symbols) {
        try {
            var prices = symbols.stream().collect(Collectors.toMap(
                Function.identity(),
                symbol -> Optional.ofNullable(currentPrices.get(symbol)).orElseThrow()
            ));
            return Optional.of(prices);
        } catch (Exception e) {
            log.warn(">>> failed to retrieve current prices for symbols {}", symbols);
            return Optional.empty();
        }
    }

    @Override
    public Optional<Order> tryToExecute(Order order) {
        try {
            var executedOrder = orderExecutors.get(order.getExecutionType()).apply(order);
            return Optional.of(executedOrder);
        } catch (Exception e) {
            log.error(">>> exception occurred during execution of order {}", order, e);
            return Optional.empty();
        }
    }

    private Order executeStubOrder(Order order) {
        var symbol = order.getSymbol();
        var orderType = order.getType();
        return getCurrentPrices(List.of(symbol))
            .map(currentPrices -> currentPrices.get(symbol))
            .map(currentPrices -> currentPrices.get(orderType))
            .map(price -> order.toBuilder().price(price).build())
            .orElseThrow(() -> new RuntimeException(
                String.format(">>> failed to retrieve current price for symbol [%s]", symbol)));
    }

    private Order executeMarketOrder(Order order) {
        var orderExecutionResult = performOrderExecution(order);
        log.debug(">>> market order has been executed: {}", orderExecutionResult);
        return formExecutedOrder(order, orderExecutionResult);
    }

    @SneakyThrows
    private List<List<Object>> performSeriesDataRetrieval(String symbol, Interval interval, Integer seriesLength) {
        var seriesApiUri = configurationProperties.getSeriesUri();
        var intervalRepresentation = configurationProperties.getIntervalRepresentation(interval);
        var limit = seriesLength + 1; // take one more bar since the last bar is incomplete
        var uri = UriComponentsBuilder.fromHttpUrl(seriesApiUri)
            .queryParam("symbol", symbol)
            .queryParam("interval", intervalRepresentation)
            .queryParam("limit", limit)
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

    private Optional<BarSeries> createSeries(List<List<Object>> seriesData, Interval interval) {
        var bars = seriesData.stream()
            .<Bar>map(barData -> {
                var duration = Duration.ZERO;
                var closeTime = getCloseTime(barData, interval);
                var open = String.valueOf(barData.get(OPEN_PRICE_INDEX));
                var high = String.valueOf(barData.get(HIGH_PRICE_INDEX));
                var low = String.valueOf(barData.get(LOW_PRICE_INDEX));
                var close = String.valueOf(barData.get(CLOSE_PRICE_INDEX));
                var volume = String.valueOf(barData.get(VOLUME_INDEX));
                return new BaseBar(duration, closeTime, open, high, low, close, volume);
            })
            .limit(seriesData.size() - 1) // skip the last bar since it is incomplete
            .collect(toList());
        return Optional.of(new BaseBarSeries(bars));
    }

    private ZonedDateTime getCloseTime(List<Object> barData, Interval interval) {
        var openTimestamp = Long.parseLong(String.valueOf(barData.get(TIMESTAMP_INDEX)));
        var closeTimestamp = openTimestamp + interval.inMillis();
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(closeTimestamp), ZoneId.systemDefault());
    }

    private String buildOrderRequestBody(Order order) {
        var requestBody = Map.of(
            "timeInForce", "FOK",
            "recvWindow", "5000",
            "type", "MARKET",
            "symbol", order.getSymbol(),
            "quantity", order.getAmount().toString(),
            "timestamp", String.valueOf(clock.millis()),
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
        var amount = getAmountAtOrderExecutionTime(orderExecutionResult, order);
        var price = getPriceAtOrderExecutionTime(orderExecutionResult);
        return order.toBuilder().amount(amount).price(price).build();
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

    private Double getAmountAtOrderExecutionTime(OrderExecutionResult orderExecutionResult, Order order) {
        var originalAmount = order.getAmount();
        var amountAtOrderExecutionTime = Double.valueOf(orderExecutionResult.getExecutedQty());
        if (!originalAmount.equals(amountAtOrderExecutionTime)) {
            log.warn(">>> original amount [{}] is different from amount at order execution time [{}].",
                originalAmount, amountAtOrderExecutionTime);
        }
        return amountAtOrderExecutionTime;
    }

    private Double getPriceAtOrderExecutionTime(OrderExecutionResult orderExecutionResult) {
        return Double.valueOf(orderExecutionResult.getFills().get(0).getPrice());
    }

    @SneakyThrows
    public void startNewCurrentPriceProvider() {
        currentPrices.clear();
        if (currentPriceProvider != null) {
            TimeUnit.MINUTES.sleep(1);
        }
        currentPriceProvider = currentPriceProviderFactory.create(this);
    }

    public void acceptCurrentPrices(Map<String, Map<OrderType, Double>> prices) {
        currentPrices.putAll(prices);
    }
}
