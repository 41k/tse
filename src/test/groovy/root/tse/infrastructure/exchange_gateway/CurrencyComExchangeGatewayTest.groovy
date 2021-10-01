package root.tse.infrastructure.exchange_gateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.util.concurrent.RateLimiter
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.RestTemplate
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties
import root.tse.domain.strategy_execution.Interval
import root.tse.domain.strategy_execution.trade.Order
import root.tse.domain.strategy_execution.trade.OrderType
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import static root.tse.configuration.ExchangeGatewayConfiguration.buildRetryTemplate
import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED
import static root.tse.domain.strategy_execution.trade.OrderStatus.NOT_FILLED

class CurrencyComExchangeGatewayTest extends Specification {

    private static final API_KEY = 'api-key-1'
    private static final SECRET_KEY = 'secret-key-1'
    private static final BASE_API_URL = 'https://api-exchange-gateway.com'
    private static final SERIES_URI = "$BASE_API_URL/klines"
    private static final ORDER_URI = "$BASE_API_URL/order"
    private static final INTERVAL = Interval.ONE_DAY
    private static final INTERVAL_REPRESENTATION = '1d'
    private static final INTERVAL_TO_REPRESENTATION_MAP = [(INTERVAL) : INTERVAL_REPRESENTATION]
    private static final RETRY_ATTEMPTS_NUMBER = 3
    private static final RETRY_BACKOFF_IN_MILLISECONDS = 50
    private static final CONFIGURATION_PROPERTIES = ExchangeGatewayConfigurationProperties.builder()
        .apiKey(API_KEY).secretKey(SECRET_KEY).seriesUri(SERIES_URI).orderUri(ORDER_URI)
        .intervalToRepresentationMap(INTERVAL_TO_REPRESENTATION_MAP).retryAttemptsNumber(RETRY_ATTEMPTS_NUMBER)
        .retryBackoffInMilliseconds(RETRY_BACKOFF_IN_MILLISECONDS).build()

    private static final T1 = 1632963600000L
    private static final O1 = 2849.87d
    private static final H1 = 3049.99d
    private static final L1 = 2836.74d
    private static final C1 = 3001.02d
    private static final V1 = 565295d

    private static final T2 = 1633050000000L
    private static final O2 = 3000.72d
    private static final H2 = 3326.81d
    private static final L2 = 2966.04d
    private static final C2 = 3310.91d
    private static final V2 = 448463d

    private static final SYMBOL = 'symbol-1'
    private static final SERIES_LENGTH = 2
    private static final SERIES_DATA = "[" +
        "[$T1, \"$O1\", \"$H1\", \"$L1\", \"$C1\", \"$V1\"]," +
        "[$T2, \"$O2\", \"$H2\", \"$L2\", \"$C2\", \"$V2\"]" +
    "]"
    private static final ENRICHED_SERIES_URI = "$SERIES_URI?symbol=$SYMBOL&interval=$INTERVAL_REPRESENTATION&limit=$SERIES_LENGTH"

    private static final ORDER_TYPE = OrderType.BUY
    private static final PRICE = 2000.0d
    private static final PRICE_AT_ORDER_EXECUTION_TIME = 2100.0d
    private static final AMOUNT = 2.0d
    private static final AMOUNT_AT_ORDER_EXECUTION_TIME = 1.0d
    private static final TIMESTAMP = 100000L
    private static final ORDER_TO_EXECUTE = Order.builder()
        .type(ORDER_TYPE).symbol(SYMBOL).amount(AMOUNT).price(PRICE).timestamp(TIMESTAMP).build()
    private static final ORDER_EXECUTION_REQUEST_BODY =
        "quantity=$AMOUNT&recvWindow=5000&side=$ORDER_TYPE" +
        "&symbol=$SYMBOL&timeInForce=FOK&timestamp=$TIMESTAMP&type=MARKET" +
        "&signature=e990f38a1aa8bf222cd7866982bbc432cdd6b9cdbb06e159a377d8f4071d0161" as String
    private static final ORDER_EXECUTION_REQUEST_HEADERS = {
        def headers = new HttpHeaders()
        headers.setContentType(APPLICATION_FORM_URLENCODED)
        headers.add('X-MBX-APIKEY', API_KEY)
        return headers
    }
    private static final ORDER_EXECUTION_REQUEST = new HttpEntity(ORDER_EXECUTION_REQUEST_BODY, ORDER_EXECUTION_REQUEST_HEADERS())
    private static final ORDER_EXECUTION_RESPONSE_BODY = { status -> "{" +
        "\"status\": \"$status\"," +
        "\"executedQty\": \"$AMOUNT_AT_ORDER_EXECUTION_TIME\"," +
        "\"fills\": [{" +
            "\"price\": \"$PRICE_AT_ORDER_EXECUTION_TIME\"" +
        "}]" +
    "}" as String }
    private static final VALID_ORDER_EXECUTION_RESPONSE_BODY = ORDER_EXECUTION_RESPONSE_BODY('FILLED')
    private static final INVALID_ORDER_EXECUTION_RESPONSE_BODY = ORDER_EXECUTION_RESPONSE_BODY('REJECTED')

    private retryTemplate = buildRetryTemplate(CONFIGURATION_PROPERTIES)
    private rateLimiter = Mock(RateLimiter)
    private restTemplate = Mock(RestTemplate)
    private objectMapper = new ObjectMapper()

    private exchangeGateway = new CurrencyComExchangeGateway(
        CONFIGURATION_PROPERTIES, retryTemplate, rateLimiter, restTemplate, objectMapper)


    // ---
    // series retrieval tests set
    // ---

    def 'should get series successfully with retries'() {
        when:
        def seriesOptional = exchangeGateway.getSeries(SYMBOL, INTERVAL, SERIES_LENGTH)

        then:
        3 * rateLimiter.acquire()
        3 * restTemplate.exchange(ENRICHED_SERIES_URI, HttpMethod.GET, HttpEntity.EMPTY, String.class) >>
            { throw new RuntimeException() } >>
            { throw new RuntimeException() } >>
            new ResponseEntity(SERIES_DATA, HttpStatus.OK)
        0 * _

        and:
        def series = seriesOptional.get()
        series.getBarCount() == SERIES_LENGTH

        and:
        def bar1 = series.getBar(0)
        bar1.getBeginTime().toInstant().toEpochMilli() == T1
        bar1.getOpenPrice().doubleValue() == O1
        bar1.getHighPrice().doubleValue() == H1
        bar1.getLowPrice().doubleValue() == L1
        bar1.getClosePrice().doubleValue() == C1
        bar1.getVolume().doubleValue() == V1

        and:
        def bar2 = series.getBar(1)
        bar2.getBeginTime().toInstant().toEpochMilli() == T2
        bar2.getOpenPrice().doubleValue() == O2
        bar2.getHighPrice().doubleValue() == H2
        bar2.getLowPrice().doubleValue() == L2
        bar2.getClosePrice().doubleValue() == C2
        bar2.getVolume().doubleValue() == V2

        and:
        noExceptionThrown()
    }

    def 'should return empty optional in case of series retrieval 3rd party calls failure'() {
        when:
        def seriesOptional = exchangeGateway.getSeries(SYMBOL, INTERVAL, SERIES_LENGTH)

        then:
        3 * rateLimiter.acquire()
        3 * restTemplate.exchange(ENRICHED_SERIES_URI, HttpMethod.GET, HttpEntity.EMPTY, String.class) >>
            { throw new RuntimeException() } >>
            { throw new RuntimeException() } >>
            { throw new RuntimeException() }
        0 * _

        and:
        seriesOptional.isEmpty()

        and:
        noExceptionThrown()
    }

    def 'should return empty optional in case of invalid response from 3rd party'() {
        when:
        def seriesOptional = exchangeGateway.getSeries(SYMBOL, INTERVAL, SERIES_LENGTH)

        then:
        1 * rateLimiter.acquire()
        1 * restTemplate.exchange(ENRICHED_SERIES_URI, HttpMethod.GET, HttpEntity.EMPTY, String.class, []) >> {
            return new ResponseEntity(HttpStatus.OK)
        }
        0 * _

        and:
        seriesOptional.isEmpty()

        and:
        noExceptionThrown()
    }

    def 'should return empty optional if interval representation is not configured'() {
        when:
        def intervalWithoutConfiguredRepresentation = Interval.FOUR_HOURS
        def seriesOptional = exchangeGateway.getSeries(SYMBOL, intervalWithoutConfiguredRepresentation, SERIES_LENGTH)

        then:
        0 * _

        and:
        seriesOptional.isEmpty()

        and:
        noExceptionThrown()
    }


    // ---
    // order execution tests set //
    // ---

    def 'should execute order successfully with retries'() {
        when:
        def executedOrder = exchangeGateway.execute(ORDER_TO_EXECUTE)

        then:
        3 * rateLimiter.acquire()
        3 * restTemplate.exchange(ORDER_URI, HttpMethod.POST, ORDER_EXECUTION_REQUEST, String) >>
            { throw new RuntimeException() } >>
            { throw new RuntimeException() } >>
            new ResponseEntity(VALID_ORDER_EXECUTION_RESPONSE_BODY, HttpStatus.OK)
        0 * _

        and:
        executedOrder.status == FILLED
        executedOrder.type == ORDER_TYPE
        executedOrder.symbol == SYMBOL
        executedOrder.amount == AMOUNT_AT_ORDER_EXECUTION_TIME
        executedOrder.price == PRICE_AT_ORDER_EXECUTION_TIME
        executedOrder.timestamp == TIMESTAMP

        and:
        noExceptionThrown()
    }

    def 'should return NOT FILLED order in case of order execution 3rd party calls failure'() {
        when:
        def executedOrder = exchangeGateway.execute(ORDER_TO_EXECUTE)

        then:
        3 * rateLimiter.acquire()
        3 * restTemplate.exchange(ORDER_URI, HttpMethod.POST, ORDER_EXECUTION_REQUEST, String) >>
            { throw new RuntimeException() } >>
            { throw new RuntimeException() } >>
            { throw new RuntimeException() }
        0 * _

        and:
        executedOrder.status == NOT_FILLED
        executedOrder.type == ORDER_TYPE
        executedOrder.symbol == SYMBOL
        executedOrder.amount == AMOUNT
        executedOrder.price == PRICE
        executedOrder.timestamp == TIMESTAMP

        and:
        noExceptionThrown()
    }

    @Unroll
    def 'should return NOT FILLED order in case of invalid response from 3rd party'() {
        when:
        def executedOrder = exchangeGateway.execute(ORDER_TO_EXECUTE)

        then:
        1 * rateLimiter.acquire()
        1 * restTemplate.exchange(ORDER_URI, HttpMethod.POST, ORDER_EXECUTION_REQUEST, String) >> {
            return new ResponseEntity(invalidOrderExecutionResponseBoby, HttpStatus.OK)
        }
        0 * _

        and:
        executedOrder.status == NOT_FILLED
        executedOrder.type == ORDER_TYPE
        executedOrder.symbol == SYMBOL
        executedOrder.amount == AMOUNT
        executedOrder.price == PRICE
        executedOrder.timestamp == TIMESTAMP

        and:
        noExceptionThrown()

        where:
        invalidOrderExecutionResponseBoby     || _
        null                                  || _
        INVALID_ORDER_EXECUTION_RESPONSE_BODY || _
    }
}
