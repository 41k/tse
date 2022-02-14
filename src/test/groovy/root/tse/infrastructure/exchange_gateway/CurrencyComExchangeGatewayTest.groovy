package root.tse.infrastructure.exchange_gateway

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.common.util.concurrent.RateLimiter
import org.springframework.http.*
import org.springframework.web.client.RestTemplate
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties
import root.tse.domain.clock.Interval
import root.tse.domain.order.Order
import root.tse.domain.order.OrderExecutionType
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Clock

import static org.springframework.http.MediaType.APPLICATION_FORM_URLENCODED
import static root.tse.configuration.ExchangeGatewayConfiguration.buildRetryTemplate
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL

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

    private static final OPEN_TIMESTAMP_1 = 1632963600000L
    private static final CLOSE_TIMESTAMP_1 = OPEN_TIMESTAMP_1 + INTERVAL.inMillis()
    private static final O1 = 2849.87d
    private static final H1 = 3049.99d
    private static final L1 = 2836.74d
    private static final C1 = 3001.02d
    private static final V1 = 565295d

    private static final OPEN_TIMESTAMP_2 = 1633050000000L
    private static final CLOSE_TIMESTAMP_2 = OPEN_TIMESTAMP_2 + INTERVAL.inMillis()
    private static final O2 = 3000.72d
    private static final H2 = 3326.81d
    private static final L2 = 2966.04d
    private static final C2 = 3310.91d
    private static final V2 = 448463d

    private static final OPEN_TIMESTAMP_3 = 1633136400000L
    private static final CLOSE_TIMESTAMP_3 = OPEN_TIMESTAMP_3 + INTERVAL.inMillis()
    private static final O3 = 3310.91d
    private static final H3 = 3573.06d
    private static final L3 = 3245.17d
    private static final C3 = 3470.95d
    private static final V3 = 475461d

    private static final SYMBOL = 'symbol-1'
    private static final SERIES_LENGTH = 2
    private static final LIMIT = SERIES_LENGTH + 1
    private static final SERIES_DATA = "[" +
        "[$OPEN_TIMESTAMP_1, \"$O1\", \"$H1\", \"$L1\", \"$C1\", \"$V1\"]," +
        "[$OPEN_TIMESTAMP_2, \"$O2\", \"$H2\", \"$L2\", \"$C2\", \"$V2\"]," +
        "[$OPEN_TIMESTAMP_3, \"$O3\", \"$H3\", \"$L3\", \"$C3\", \"$V3\"]" +
    "]"
    private static final ENRICHED_SERIES_URI = "$SERIES_URI?symbol=$SYMBOL&interval=$INTERVAL_REPRESENTATION&limit=$LIMIT"

    private static final ORDER_TYPE = BUY
    private static final PRICE_AT_ORDER_EXECUTION_TIME = 2100.0d
    private static final AMOUNT = 2.0d
    private static final AMOUNT_AT_ORDER_EXECUTION_TIME = 1.0d
    private static final TIMESTAMP = 100000L
    private static final ORDER_TO_EXECUTE = Order.builder().type(ORDER_TYPE).symbol(SYMBOL).amount(AMOUNT).build()
    private static final STUB_ORDER_TO_EXECUTE = ORDER_TO_EXECUTE.toBuilder().executionType(OrderExecutionType.STUB).build()
    private static final MARKET_ORDER_TO_EXECUTE = ORDER_TO_EXECUTE.toBuilder().executionType(OrderExecutionType.MARKET).build()
    private static final MARKET_ORDER_EXECUTION_REQUEST_BODY =
        "quantity=$AMOUNT&recvWindow=5000&side=$ORDER_TYPE" +
        "&symbol=$SYMBOL&timeInForce=FOK&timestamp=$TIMESTAMP&type=MARKET" +
        "&signature=e990f38a1aa8bf222cd7866982bbc432cdd6b9cdbb06e159a377d8f4071d0161" as String
    private static final ORDER_EXECUTION_REQUEST_HEADERS = {
        def headers = new HttpHeaders()
        headers.setContentType(APPLICATION_FORM_URLENCODED)
        headers.add('X-MBX-APIKEY', API_KEY)
        return headers
    }
    private static final MARKET_ORDER_EXECUTION_REQUEST = new HttpEntity(MARKET_ORDER_EXECUTION_REQUEST_BODY, ORDER_EXECUTION_REQUEST_HEADERS())
    private static final ORDER_EXECUTION_RESPONSE_BODY = { status -> "{" +
        "\"status\": \"$status\"," +
        "\"executedQty\": \"$AMOUNT_AT_ORDER_EXECUTION_TIME\"," +
        "\"fills\": [{" +
            "\"price\": \"$PRICE_AT_ORDER_EXECUTION_TIME\"" +
        "}]" +
    "}" as String }
    private static final VALID_ORDER_EXECUTION_RESPONSE_BODY = ORDER_EXECUTION_RESPONSE_BODY('FILLED')
    private static final INVALID_ORDER_EXECUTION_RESPONSE_BODY = ORDER_EXECUTION_RESPONSE_BODY('REJECTED')

    private currentPriceProviderFactory = Mock(CurrentPriceProviderFactory)
    private retryTemplate = buildRetryTemplate(CONFIGURATION_PROPERTIES)
    private rateLimiter = Mock(RateLimiter)
    private restTemplate = Mock(RestTemplate)
    private objectMapper = new ObjectMapper()
    private clock = Mock(Clock)

    private CurrencyComExchangeGateway exchangeGateway

    def setup() {
        exchangeGateway = new CurrencyComExchangeGateway(
            CONFIGURATION_PROPERTIES, currentPriceProviderFactory,
            retryTemplate, rateLimiter, restTemplate, objectMapper, clock)
    }


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
        bar1.getEndTime().toInstant().toEpochMilli() == CLOSE_TIMESTAMP_1
        bar1.getOpenPrice().doubleValue() == O1
        bar1.getHighPrice().doubleValue() == H1
        bar1.getLowPrice().doubleValue() == L1
        bar1.getClosePrice().doubleValue() == C1
        bar1.getVolume().doubleValue() == V1

        and:
        def bar2 = series.getBar(1)
        bar2.getEndTime().toInstant().toEpochMilli() == CLOSE_TIMESTAMP_2
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
    // current price retrieval tests set
    // ---

    def 'should initialize exchange gateway correctly'() {
        given:
        def currentPriceProvider = Mock(CurrentPriceProvider)

        when:
        def gateway = new CurrencyComExchangeGateway(
            CONFIGURATION_PROPERTIES, currentPriceProviderFactory,
            retryTemplate, rateLimiter, restTemplate, objectMapper, clock)

        then:
        1 * currentPriceProviderFactory.create(_) >> currentPriceProvider
        0 * _

        and:
        gateway.currentPrices.isEmpty()
        gateway.currentPriceProvider == currentPriceProvider
    }

    def 'should accept current prices'() {
        given:
        def symbol1 = 'symbol-1'
        def symbol2 = 'symbol-2'

        and:
        assert exchangeGateway.currentPrices.isEmpty()

        and:
        exchangeGateway.currentPrices.put((symbol1), [(BUY) : 5d, (SELL) : 4d])

        when:
        exchangeGateway.acceptCurrentPrices([(symbol2) : [(BUY) : 12d, (SELL) : 11d]])

        then:
        exchangeGateway.currentPrices == [
            (symbol1) : [(BUY) : 5d, (SELL) : 4d],
            (symbol2) : [(BUY) : 12d, (SELL) : 11d]
        ]

        when:
        exchangeGateway.acceptCurrentPrices([(symbol2) : [(BUY) : 10d, (SELL) : 9d]])

        then:
        exchangeGateway.currentPrices == [
            (symbol1) : [(BUY) : 5d, (SELL) : 4d],
            (symbol2) : [(BUY) : 10d, (SELL) : 9d]
        ]
    }

    def 'should provide current prices successfully'() {
        given:
        def symbol1 = 'symbol-1'
        def symbol2 = 'symbol-2'
        def symbol3 = 'symbol-3'
        exchangeGateway.currentPrices.putAll([
            (symbol1): [(BUY): 102d, (SELL): 101d],
            (symbol2): [(BUY): 7d, (SELL): 6d],
            (symbol3): [(BUY): 1003d, (SELL): 1000d]
        ])

        expect:
        exchangeGateway.getCurrentPrices([symbol1, symbol3]) == Optional.of([
            (symbol1): [(BUY): 102d, (SELL): 101d],
            (symbol3): [(BUY): 1003d, (SELL): 1000d]
        ])
    }

    def 'should return empty optional if there are no prices for at least one symbol'() {
        given:
        def symbol1 = 'symbol-1'
        def symbol2 = 'symbol-2'
        exchangeGateway.currentPrices.putAll([
            (symbol1): [(BUY): 102d, (SELL): 101d]
        ])

        expect:
        exchangeGateway.getCurrentPrices([symbol1, symbol2]) == Optional.empty()
    }


    // ---
    // order execution tests set
    // ---

    def 'should execute stub order successfully'() {
        given:
        def buyPrice = 5d
        def sellPrice = 4d
        exchangeGateway.currentPrices.putAll([(SYMBOL) : [(BUY) : buyPrice, (SELL) : sellPrice]])

        when:
        def executedOrder = exchangeGateway.tryToExecute(STUB_ORDER_TO_EXECUTE).get()

        then:
        0 * _

        and:
        executedOrder.price == buyPrice
    }

    def 'should not execute stub order if current prices were not obtained'() {
        given:
        assert exchangeGateway.currentPrices.isEmpty()

        when:
        def executedOrderOptional = exchangeGateway.tryToExecute(STUB_ORDER_TO_EXECUTE)

        then:
        0 * _

        and:
        executedOrderOptional.isEmpty()
    }

    def 'should execute market order successfully with retries'() {
        when:
        def executedOrder = exchangeGateway.tryToExecute(MARKET_ORDER_TO_EXECUTE).get()

        then:
        1 * clock.millis() >> TIMESTAMP
        3 * rateLimiter.acquire()
        3 * restTemplate.exchange(ORDER_URI, HttpMethod.POST, MARKET_ORDER_EXECUTION_REQUEST, String) >>
            { throw new RuntimeException() } >>
            { throw new RuntimeException() } >>
            new ResponseEntity(VALID_ORDER_EXECUTION_RESPONSE_BODY, HttpStatus.OK)
        0 * _

        and:
        executedOrder.type == ORDER_TYPE
        executedOrder.symbol == SYMBOL
        executedOrder.amount == AMOUNT_AT_ORDER_EXECUTION_TIME
        executedOrder.price == PRICE_AT_ORDER_EXECUTION_TIME

        and:
        noExceptionThrown()
    }

    def 'should return NOT FILLED market order in case of order execution 3rd party calls failure'() {
        when:
        def executedOrderOptional = exchangeGateway.tryToExecute(MARKET_ORDER_TO_EXECUTE)

        then:
        1 * clock.millis() >> TIMESTAMP
        3 * rateLimiter.acquire()
        3 * restTemplate.exchange(ORDER_URI, HttpMethod.POST, MARKET_ORDER_EXECUTION_REQUEST, String) >>
            { throw new RuntimeException() } >>
            { throw new RuntimeException() } >>
            { throw new RuntimeException() }
        0 * _

        and:
        executedOrderOptional.isEmpty()

        and:
        noExceptionThrown()
    }

    @Unroll
    def 'should return NOT FILLED market order in case of invalid response from 3rd party'() {
        when:
        def executedOrderOptional = exchangeGateway.tryToExecute(MARKET_ORDER_TO_EXECUTE)

        then:
        1 * clock.millis() >> TIMESTAMP
        1 * rateLimiter.acquire()
        1 * restTemplate.exchange(ORDER_URI, HttpMethod.POST, MARKET_ORDER_EXECUTION_REQUEST, String) >> {
            return new ResponseEntity(invalidOrderExecutionResponseBoby, HttpStatus.OK)
        }
        0 * _

        and:
        executedOrderOptional.isEmpty()

        and:
        noExceptionThrown()

        where:
        invalidOrderExecutionResponseBoby     || _
        null                                  || _
        INVALID_ORDER_EXECUTION_RESPONSE_BODY || _
    }
}
