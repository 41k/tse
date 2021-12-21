package root.tse.domain.strategy_execution

import com.github.tomakehurst.wiremock.WireMockServer
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import root.TseApp
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.rule.RuleCheckResult
import root.tse.domain.strategy_execution.trade.Order
import root.tse.domain.strategy_execution.trade.TradeType
import root.tse.infrastructure.clock.ClockSignalPropagator
import root.tse.infrastructure.persistence.trade.TradeDbEntryJpaRepository
import spock.lang.Specification

import static com.github.tomakehurst.wiremock.client.WireMock.*
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import static com.google.common.io.Resources.getResource
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT
import static root.tse.domain.strategy_execution.trade.TradeType.LONG
import static root.tse.util.TestUtils.createClockSignal

@SpringBootTest(webEnvironment = RANDOM_PORT)
@ContextConfiguration(classes = [TseApp, BaseTestContextConfiguration])
abstract class BaseStrategyExecutionFunctionalTest extends Specification {

    protected static final SERIES_LENGTH = 3
    protected static final LIMIT = SERIES_LENGTH + 1
    protected static final SYMBOL_1 = 'symbol-1'
    protected static final SYMBOL_2 = 'symbol-2'
    protected static final SYMBOLS = [SYMBOL_1, SYMBOL_2]
    protected static final AMOUNT_1 = 20d
    protected static final AMOUNT_2 = 10d
    protected static final PRICE_1 = 10.0d
    protected static final PRICE_2 = 20.0d
    protected static final TIMESTAMP_1 = 1634025720000L
    protected static final TIMESTAMP_2 = 1634025780000L
    protected static final FUNDS_PER_TRADE = 200d
    protected static final TRANSACTION_FEE_PERCENT = 0.2d
    protected static final NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES = 2
    protected static final ENTRY_RULE_CLOCK_SIGNAL_INTERVAL = Interval.FIVE_MINUTES
    protected static final ENTRY_RULE_CLOCK_SIGNAL = createClockSignal(ENTRY_RULE_CLOCK_SIGNAL_INTERVAL, TIMESTAMP_1)
    protected static final EXIT_RULE_CLOCK_SIGNAL_INTERVAL = Interval.ONE_MINUTE
    protected static final EXIT_RULE_CLOCK_SIGNAL = createClockSignal(EXIT_RULE_CLOCK_SIGNAL_INTERVAL, TIMESTAMP_2)
    protected static final EXIT_RULE_CLOCK_SIGNAL_WITH_ENTRY_ORDER_CLOCK_SIGNAL_TIMESTAMP = createClockSignal(EXIT_RULE_CLOCK_SIGNAL_INTERVAL, TIMESTAMP_1)
    protected static final NOT_REQUIRED_CLOCK_SIGNAL = createClockSignal(Interval.FIFTEEN_MINUTES, TIMESTAMP_2)

    protected static final SERIES_RETRIEVAL_RESPONSE_TEMPLATE = getResource('json/series-retrieval-response.json').text
    protected static final ORDER_EXECUTION_REQUEST_TEMPLATE = 'quantity=$AMOUNT&recvWindow=5000&side=$ORDER_TYPE&symbol=$SYMBOL&timeInForce=FOK&timestamp=$TIMESTAMP&type=MARKET&signature=$SIGNATURE'
    protected static final ORDER_EXECUTION_RESPONSE_TEMPLATE = getResource('json/order-execution-response.json').text

    private static final ENTRY_RULE_SERIES_RETRIEVAL_RESPONSE = [
        (SYMBOL_1) : SERIES_RETRIEVAL_RESPONSE_TEMPLATE
                        .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                        .replace('$CLOSE_PRICE', PRICE_1 as String),
        (SYMBOL_2) : SERIES_RETRIEVAL_RESPONSE_TEMPLATE
                        .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                        .replace('$CLOSE_PRICE', PRICE_2 as String)
    ]
    private static final EXIT_RULE_SERIES_RETRIEVAL_RESPONSE = [
        (SYMBOL_1) : SERIES_RETRIEVAL_RESPONSE_TEMPLATE
                        .replace('$TIMESTAMP', TIMESTAMP_2 as String)
                        .replace('$CLOSE_PRICE', PRICE_2 as String),
        (SYMBOL_2) : SERIES_RETRIEVAL_RESPONSE_TEMPLATE
                        .replace('$TIMESTAMP', TIMESTAMP_2 as String)
                        .replace('$CLOSE_PRICE', PRICE_1 as String)
    ]
    private static final ENTRY_ORDER_EXECUTION_REQUEST = [
        (SYMBOL_1) : ORDER_EXECUTION_REQUEST_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_1 as String)
                        .replace('$ORDER_TYPE', 'BUY')
                        .replace('$SYMBOL', SYMBOL_1)
                        .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                        .replace('$SIGNATURE', '21c70bafe834a617bc9eba5e9a219fa2550ec57812378abfd411dacd031e08e6'),
        (SYMBOL_2) : ORDER_EXECUTION_REQUEST_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_2 as String)
                        .replace('$ORDER_TYPE', 'BUY')
                        .replace('$SYMBOL', SYMBOL_2)
                        .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                        .replace('$SIGNATURE', 'b89af5a0949de48951a121a95f1fd460b5a31d10d13f52ce37a03090cec0c7e2')
    ]
    private static final ENTRY_ORDER_EXECUTION_RESPONSE = [
        (SYMBOL_1) : ORDER_EXECUTION_RESPONSE_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_1 as String)
                        .replace('$PRICE', PRICE_1 as String),
        (SYMBOL_2) : ORDER_EXECUTION_RESPONSE_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_2 as String)
                        .replace('$PRICE', PRICE_2 as String)
    ]
    private static final EXIT_ORDER_EXECUTION_REQUEST = [
        (SYMBOL_1) : ORDER_EXECUTION_REQUEST_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_1 as String)
                        .replace('$ORDER_TYPE', 'SELL')
                        .replace('$SYMBOL', SYMBOL_1)
                        .replace('$TIMESTAMP', TIMESTAMP_2 as String)
                        .replace('$SIGNATURE', 'cb87e0f4fa495b463271bba13473c616892600247dc2fa6ef6538c0db0a0421e'),
        (SYMBOL_2) : ORDER_EXECUTION_REQUEST_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_2 as String)
                        .replace('$ORDER_TYPE', 'SELL')
                        .replace('$SYMBOL', SYMBOL_2)
                        .replace('$TIMESTAMP', TIMESTAMP_2 as String)
                        .replace('$SIGNATURE', 'a8a21af990b05a19aad0a219d5de648a7fb3651dca4c5550453391c16953b417')
    ]
    private static final EXIT_ORDER_EXECUTION_RESPONSE = [
        (SYMBOL_1) : ORDER_EXECUTION_RESPONSE_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_1 as String)
                        .replace('$PRICE', PRICE_2 as String),
        (SYMBOL_2) : ORDER_EXECUTION_RESPONSE_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_2 as String)
                        .replace('$PRICE', PRICE_1 as String)
    ]

    @SpringBean // necessary since we should propagate clock signal manually during test
    ClockSignalPropagator clockSignalPropagator = Mock()

    @Autowired
    protected TradeDbEntryJpaRepository tradeDbEntryJpaRepository

    @Autowired
    protected ClockSignalDispatcher clockSignalDispatcher

    @Autowired
    protected ExchangeGatewayConfigurationProperties exchangeGatewayConfigurationProperties

    @Autowired
    private WireMockServer wireMockServer

    def setup() {
        tradeDbEntryJpaRepository.deleteAll()
    }

    void mockSuccessfulSeriesRetrievalForEntryRule(String symbol) {
        def responseBody = ENTRY_RULE_SERIES_RETRIEVAL_RESPONSE.get(symbol)
        mockSuccessfulSeriesRetrievalCall(symbol, ENTRY_RULE_CLOCK_SIGNAL_INTERVAL, responseBody)
    }

    void mockFailedSeriesRetrievalForEntryRule(String symbol) {
        mockFailedSeriesRetrievalCall(symbol, ENTRY_RULE_CLOCK_SIGNAL_INTERVAL)
    }

    void mockSuccessfulSeriesRetrievalForExitRule(String symbol) {
        def responseBody = EXIT_RULE_SERIES_RETRIEVAL_RESPONSE.get(symbol)
        mockSuccessfulSeriesRetrievalCall(symbol, EXIT_RULE_CLOCK_SIGNAL_INTERVAL, responseBody)
    }

    void mockFailedSeriesRetrievalForExitRule(String symbol) {
        mockFailedSeriesRetrievalCall(symbol, EXIT_RULE_CLOCK_SIGNAL_INTERVAL)
    }

    void mockSuccessfulEntryOrderExecution(String symbol) {
        def requestBody = ENTRY_ORDER_EXECUTION_REQUEST.get(symbol)
        def responseBody = ENTRY_ORDER_EXECUTION_RESPONSE.get(symbol)
        mockSuccessfulOrderExecutionCall(requestBody, responseBody)
    }

    void mockFailedEntryOrderExecution(String symbol) {
        def requestBody = ENTRY_ORDER_EXECUTION_REQUEST.get(symbol)
        mockFailedOrderExecutionCall(requestBody)
    }

    void mockSuccessfulExitOrderExecution(String symbol) {
        def requestBody = EXIT_ORDER_EXECUTION_REQUEST.get(symbol)
        def responseBody = EXIT_ORDER_EXECUTION_RESPONSE.get(symbol)
        mockSuccessfulOrderExecutionCall(requestBody, responseBody)
    }

    void mockFailedExitOrderExecution(String symbol) {
        def requestBody = EXIT_ORDER_EXECUTION_REQUEST.get(symbol)
        mockFailedOrderExecutionCall(requestBody)
    }

    void mockSuccessfulSeriesRetrievalCall(String symbol, Interval interval, String responseBody) {
        wireMockServer.stubFor(get(urlEqualTo(seriesRetrievalUrl(symbol, interval)))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(responseBody)
                .withHeader('Connection', 'close')))
    }

    void mockFailedSeriesRetrievalCall(String symbol, Interval interval) {
        wireMockServer.stubFor(get(urlEqualTo(seriesRetrievalUrl(symbol, interval)))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader('Connection', 'close')))
    }

    String seriesRetrievalUrl(String symbol, Interval interval) {
        def intervalRepresentation = exchangeGatewayConfigurationProperties.getIntervalRepresentation(interval)
        "/klines?symbol=$symbol&interval=$intervalRepresentation&limit=$LIMIT"
    }

    void mockSuccessfulOrderExecutionCall(String requestBody, String responseBody) {
        wireMockServer.stubFor(post(urlPathEqualTo('/order'))
            .withHeader('Content-Type', equalTo('application/x-www-form-urlencoded'))
            .withHeader('X-MBX-APIKEY', equalTo(exchangeGatewayConfigurationProperties.getApiKey()))
            .withRequestBody(equalTo(requestBody))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(responseBody)
                .withHeader('Connection', 'close')))
    }

    void mockFailedOrderExecutionCall(String requestBody) {
        wireMockServer.stubFor(post(urlPathEqualTo('/order'))
            .withHeader('Content-Type', equalTo('application/x-www-form-urlencoded'))
            .withHeader('X-MBX-APIKEY', equalTo(exchangeGatewayConfigurationProperties.getApiKey()))
            .withRequestBody(equalTo(requestBody))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader('Connection', 'close')))
    }

    @TestConfiguration
    static class BaseTestContextConfiguration {
        @Bean
        WireMockServer wireMockServer() {
            def wireMockServer = new WireMockServer(wireMockConfig().port(7777))
            wireMockServer.start()
            wireMockServer
        }
    }

    static class SampleStrategy implements Strategy {

        private final ExchangeGateway exchangeGateway

        SampleStrategy(ExchangeGateway exchangeGateway) {
            this.exchangeGateway = exchangeGateway
        }

        @Override
        String getId() { 'strategy-id-1' }

        @Override
        String getName() { 'strategy-name-1' }

        @Override
        TradeType getTradeType() { LONG }

        @Override
        EntryRule getEntryRule() {
            new EntryRule() {
                @Override
                RuleCheckResult check(String symbol) {
                    exchangeGateway.getSeries(symbol, ENTRY_RULE_CLOCK_SIGNAL_INTERVAL, SERIES_LENGTH)
                        .map({series ->
                            def barOnWhichRuleWasSatisfied = series.getLastBar()
                            RuleCheckResult.satisfied(barOnWhichRuleWasSatisfied)
                        })
                        .orElseGet({ RuleCheckResult.notSatisfied() })
                }
                @Override
                Interval getLowestInterval() { ENTRY_RULE_CLOCK_SIGNAL_INTERVAL }
                @Override
                Interval getHighestInterval() { ENTRY_RULE_CLOCK_SIGNAL_INTERVAL }
            }
        }

        @Override
        ExitRule getExitRule() {
            new ExitRule() {
                @Override
                RuleCheckResult check(Order entryOrder) {
                    def symbol = entryOrder.getSymbol()
                    exchangeGateway.getSeries(symbol, EXIT_RULE_CLOCK_SIGNAL_INTERVAL, SERIES_LENGTH)
                        .map({series ->
                            def barOnWhichRuleWasSatisfied = series.getLastBar()
                            RuleCheckResult.satisfied(barOnWhichRuleWasSatisfied)
                        })
                        .orElseGet({ RuleCheckResult.notSatisfied() })
                }
                @Override
                Interval getLowestInterval() { EXIT_RULE_CLOCK_SIGNAL_INTERVAL }
            }
        }
    }
}
