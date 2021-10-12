package root.tse.domain.strategy_execution

import com.github.tomakehurst.wiremock.WireMockServer
import org.spockframework.spring.SpringBean
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
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
import static root.tse.domain.strategy_execution.Interval.FIVE_MINUTES
import static root.tse.domain.strategy_execution.Interval.ONE_MINUTE
import static root.tse.domain.strategy_execution.StrategyExecutionMode.TRADING
import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED
import static root.tse.domain.strategy_execution.trade.OrderType.BUY
import static root.tse.domain.strategy_execution.trade.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG

@SpringBootTest(webEnvironment = RANDOM_PORT, classes = [TseApp, TestContextConfiguration])
class StrategyExecutionFunctionalTest extends Specification {

    private static final SERIES_LENGTH = 3
    public static final SYMBOL_1 = 'symbol-1'
    public static final SYMBOL_2 = 'symbol-2'
    public static final SYMBOLS = [SYMBOL_1, SYMBOL_2] as Set
    public static final AMOUNT_1 = 20d
    public static final AMOUNT_2 = 10d
    public static final PRICE_1 = 10.0d
    public static final PRICE_2 = 20.0d
    public static final TIMESTAMP_1 = 1634025720000L
    public static final TIMESTAMP_2 = 1634025780000L
    public static final FUNDS_PER_TRADE = 200d
    public static final NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES = 2

    public static final SERIES_RETRIEVAL_RESPONSE_TEMPLATE = getResource('json/series-retrieval-response.json').text
    public static final ORDER_EXECUTION_REQUEST_TEMPLATE = 'quantity=$AMOUNT&recvWindow=5000&side=$ORDER_TYPE&symbol=$SYMBOL&timeInForce=FOK&timestamp=$TIMESTAMP&type=MARKET&signature=$SIGNATURE'
    public static final ORDER_EXECUTION_RESPONSE_TEMPLATE = getResource('json/order-execution-response.json').text

    @SpringBean // necessary since we should propagate clock signal manually during test
    ClockSignalPropagator clockSignalPropagator = Mock()

    @Autowired
    private TradeDbEntryJpaRepository tradeDbEntryJpaRepository

    @Autowired
    private ClockSignalDispatcher clockSignalDispatcher

    @Autowired
    private ExchangeGatewayConfigurationProperties exchangeGatewayConfigurationProperties

    @Autowired
    private StrategyExecution strategyExecution

    @Autowired
    private WireMockServer wireMockServer

    def setup() {
        strategyExecution.tradeExecutions.clear()
        tradeDbEntryJpaRepository.deleteAll()
    }

    def 'should trade for each scanned symbol'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        // Wiremock stubs for exchange gateway calls which are made during strategy execution for SYMBOL_1

        and: 'series retrieval for entry rule with SYMBOL_1'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_1, FIVE_MINUTES, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_1 as String).replace('$CLOSE_PRICE', PRICE_1 as String))

        and: 'entry order execution for SYMBOL_1'
        mockSuccessfulOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_1 as String)
                .replace('$ORDER_TYPE', 'BUY')
                .replace('$SYMBOL', SYMBOL_1)
                .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                .replace('$SIGNATURE', '21c70bafe834a617bc9eba5e9a219fa2550ec57812378abfd411dacd031e08e6'),
            ORDER_EXECUTION_RESPONSE_TEMPLATE
                .replace('$AMOUNT', AMOUNT_1 as String)
                .replace('$PRICE', PRICE_1 as String))

        and: 'series retrieval for exit rule with SYMBOL_1'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_1, ONE_MINUTE, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_2 as String).replace('$CLOSE_PRICE', PRICE_2 as String))

        and: 'exit order execution for SYMBOL_1'
        mockSuccessfulOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_1 as String)
                .replace('$ORDER_TYPE', 'SELL')
                .replace('$SYMBOL', SYMBOL_1)
                .replace('$TIMESTAMP', TIMESTAMP_2 as String)
                .replace('$SIGNATURE', 'cb87e0f4fa495b463271bba13473c616892600247dc2fa6ef6538c0db0a0421e'),
            ORDER_EXECUTION_RESPONSE_TEMPLATE
                .replace('$AMOUNT', AMOUNT_1 as String)
                .replace('$PRICE', PRICE_2 as String))

        // Wiremock stubs for exchange gateway calls which are made during strategy execution for SYMBOL_2

        and: 'series retrieval for entry rule with SYMBOL_2'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_2, FIVE_MINUTES, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_1 as String).replace('$CLOSE_PRICE', PRICE_2 as String))

        and: 'entry order execution for SYMBOL_2'
        mockSuccessfulOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_2 as String)
                .replace('$ORDER_TYPE', 'BUY')
                .replace('$SYMBOL', SYMBOL_2)
                .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                .replace('$SIGNATURE', 'b89af5a0949de48951a121a95f1fd460b5a31d10d13f52ce37a03090cec0c7e2'),
            ORDER_EXECUTION_RESPONSE_TEMPLATE
                .replace('$AMOUNT', AMOUNT_2 as String)
                .replace('$PRICE', PRICE_2 as String))

        and: 'series retrieval for exit rule with SYMBOL_2'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_2, ONE_MINUTE, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_2 as String).replace('$CLOSE_PRICE', PRICE_1 as String))

        and: 'exit order execution for SYMBOL_2'
        mockSuccessfulOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_2 as String)
                .replace('$ORDER_TYPE', 'SELL')
                .replace('$SYMBOL', SYMBOL_2)
                .replace('$TIMESTAMP', TIMESTAMP_2 as String)
                .replace('$SIGNATURE', 'a8a21af990b05a19aad0a219d5de648a7fb3651dca4c5550453391c16953b417'),
            ORDER_EXECUTION_RESPONSE_TEMPLATE
                .replace('$AMOUNT', AMOUNT_2 as String)
                .replace('$PRICE', PRICE_1 as String))

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(FIVE_MINUTES)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(ONE_MINUTE)

        and: 'wait for trades closing'
        sleep(2000)

        then:
        def trades = tradeDbEntryJpaRepository.findAll().sort({it.symbol})
        trades.size() == 2

        and:
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(0).entryOrderStatus == FILLED
        trades.get(0).exitOrderType == SELL
        trades.get(0).exitOrderAmount == AMOUNT_1
        trades.get(0).exitOrderPrice == PRICE_2
        trades.get(0).exitOrderTimestamp.toEpochMilli() == TIMESTAMP_2
        trades.get(0).exitOrderStatus == FILLED

        and:
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(1).entryOrderStatus == FILLED
        trades.get(1).exitOrderType == SELL
        trades.get(1).exitOrderAmount == AMOUNT_2
        trades.get(1).exitOrderPrice == PRICE_1
        trades.get(1).exitOrderTimestamp.toEpochMilli() == TIMESTAMP_2
        trades.get(1).exitOrderStatus == FILLED
    }

    def 'should not open trades if series retrieval failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        and: 'failed series retrieval for entry rule with SYMBOL_1'
        mockFailedSeriesRetrievalCall(SYMBOL_1, FIVE_MINUTES)

        and: 'failed series retrieval for entry rule with SYMBOL_2'
        mockFailedSeriesRetrievalCall(SYMBOL_2, FIVE_MINUTES)

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(FIVE_MINUTES)

        and: 'wait for trades opening'
        sleep(4000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not open trades if clock signal required by entry rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        when: 'clock signal which is not required for entry rule'
        clockSignalDispatcher.dispatch(ONE_MINUTE)

        and:
        sleep(4000)

        then: 'no trades were opened'
        tradeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not close trades if exit order execution failed'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        // Wiremock stubs for exchange gateway calls which are made during strategy execution for SYMBOL_1

        and: 'series retrieval for entry rule with SYMBOL_1'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_1, FIVE_MINUTES, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_1 as String).replace('$CLOSE_PRICE', PRICE_1 as String))

        and: 'entry order execution for SYMBOL_1'
        mockSuccessfulOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_1 as String)
                .replace('$ORDER_TYPE', 'BUY')
                .replace('$SYMBOL', SYMBOL_1)
                .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                .replace('$SIGNATURE', '21c70bafe834a617bc9eba5e9a219fa2550ec57812378abfd411dacd031e08e6'),
            ORDER_EXECUTION_RESPONSE_TEMPLATE
                .replace('$AMOUNT', AMOUNT_1 as String)
                .replace('$PRICE', PRICE_1 as String))

        and: 'series retrieval for exit rule with SYMBOL_1'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_1, ONE_MINUTE, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_2 as String).replace('$CLOSE_PRICE', PRICE_2 as String))

        and: 'failed exit order execution for SYMBOL_1'
        mockFailedOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_1 as String)
                .replace('$ORDER_TYPE', 'SELL')
                .replace('$SYMBOL', SYMBOL_1)
                .replace('$TIMESTAMP', TIMESTAMP_2 as String)
                .replace('$SIGNATURE', 'cb87e0f4fa495b463271bba13473c616892600247dc2fa6ef6538c0db0a0421e'))

        // Wiremock stubs for exchange gateway calls which are made during strategy execution for SYMBOL_2

        and: 'series retrieval for entry rule with SYMBOL_2'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_2, FIVE_MINUTES, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_1 as String).replace('$CLOSE_PRICE', PRICE_2 as String))

        and: 'entry order execution for SYMBOL_2'
        mockSuccessfulOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_2 as String)
                .replace('$ORDER_TYPE', 'BUY')
                .replace('$SYMBOL', SYMBOL_2)
                .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                .replace('$SIGNATURE', 'b89af5a0949de48951a121a95f1fd460b5a31d10d13f52ce37a03090cec0c7e2'),
            ORDER_EXECUTION_RESPONSE_TEMPLATE
                .replace('$AMOUNT', AMOUNT_2 as String)
                .replace('$PRICE', PRICE_2 as String))

        and: 'series retrieval for exit rule with SYMBOL_2'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_2, ONE_MINUTE, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_2 as String).replace('$CLOSE_PRICE', PRICE_1 as String))

        and: 'failed exit order execution for SYMBOL_2'
        mockFailedOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_2 as String)
                .replace('$ORDER_TYPE', 'SELL')
                .replace('$SYMBOL', SYMBOL_2)
                .replace('$TIMESTAMP', TIMESTAMP_2 as String)
                .replace('$SIGNATURE', 'a8a21af990b05a19aad0a219d5de648a7fb3651dca4c5550453391c16953b417'))

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(FIVE_MINUTES)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal for exit rule'
        clockSignalDispatcher.dispatch(ONE_MINUTE)

        and: 'wait for trades closing'
        sleep(2000)

        then:
        def trades = tradeDbEntryJpaRepository.findAll().sort({it.symbol})
        trades.size() == 2

        and:
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(0).entryOrderStatus == FILLED
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp
        !trades.get(0).exitOrderStatus

        and:
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(1).entryOrderStatus == FILLED
        !trades.get(1).exitOrderType
        !trades.get(1).exitOrderAmount
        !trades.get(1).exitOrderPrice
        !trades.get(1).exitOrderTimestamp
        !trades.get(1).exitOrderStatus
    }

    def 'should not close trades if clock signal required by exit rule was not dispatched'() {
        given: 'no trades for now'
        assert tradeDbEntryJpaRepository.findAll().isEmpty()

        // Wiremock stubs for exchange gateway calls which are made during strategy execution for SYMBOL_1

        and: 'series retrieval for entry rule with SYMBOL_1'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_1, FIVE_MINUTES, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_1 as String).replace('$CLOSE_PRICE', PRICE_1 as String))

        and: 'entry order execution for SYMBOL_1'
        mockSuccessfulOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_1 as String)
                .replace('$ORDER_TYPE', 'BUY')
                .replace('$SYMBOL', SYMBOL_1)
                .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                .replace('$SIGNATURE', '21c70bafe834a617bc9eba5e9a219fa2550ec57812378abfd411dacd031e08e6'),
            ORDER_EXECUTION_RESPONSE_TEMPLATE
                .replace('$AMOUNT', AMOUNT_1 as String)
                .replace('$PRICE', PRICE_1 as String))

        // Wiremock stubs for exchange gateway calls which are made during strategy execution for SYMBOL_2

        and: 'series retrieval for entry rule with SYMBOL_2'
        mockSuccessfulSeriesRetrievalCall(SYMBOL_2, FIVE_MINUTES, SERIES_RETRIEVAL_RESPONSE_TEMPLATE
            .replace('$TIMESTAMP', TIMESTAMP_1 as String).replace('$CLOSE_PRICE', PRICE_2 as String))

        and: 'entry order execution for SYMBOL_2'
        mockSuccessfulOrderExecutionCall(
            ORDER_EXECUTION_REQUEST_TEMPLATE
                .replace('$AMOUNT', AMOUNT_2 as String)
                .replace('$ORDER_TYPE', 'BUY')
                .replace('$SYMBOL', SYMBOL_2)
                .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                .replace('$SIGNATURE', 'b89af5a0949de48951a121a95f1fd460b5a31d10d13f52ce37a03090cec0c7e2'),
            ORDER_EXECUTION_RESPONSE_TEMPLATE
                .replace('$AMOUNT', AMOUNT_2 as String)
                .replace('$PRICE', PRICE_2 as String))

        when: 'clock signal for entry rule'
        clockSignalDispatcher.dispatch(FIVE_MINUTES)

        and: 'wait for trades opening'
        sleep(2000)

        and: 'clock signal which is not required for exit rule'
        clockSignalDispatcher.dispatch(FIVE_MINUTES)

        and: 'wait for trades closing'
        sleep(2000)

        then:
        def trades = tradeDbEntryJpaRepository.findAll().sort({it.symbol})
        trades.size() == 2

        and:
        trades.get(0).strategyExecutionId == strategyExecution.id
        trades.get(0).type == LONG
        trades.get(0).symbol == SYMBOL_1
        trades.get(0).entryOrderType == BUY
        trades.get(0).entryOrderAmount == AMOUNT_1
        trades.get(0).entryOrderPrice == PRICE_1
        trades.get(0).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(0).entryOrderStatus == FILLED
        !trades.get(0).exitOrderType
        !trades.get(0).exitOrderAmount
        !trades.get(0).exitOrderPrice
        !trades.get(0).exitOrderTimestamp
        !trades.get(0).exitOrderStatus

        and:
        trades.get(1).strategyExecutionId == strategyExecution.id
        trades.get(1).type == LONG
        trades.get(1).symbol == SYMBOL_2
        trades.get(1).entryOrderType == BUY
        trades.get(1).entryOrderAmount == AMOUNT_2
        trades.get(1).entryOrderPrice == PRICE_2
        trades.get(1).entryOrderTimestamp.toEpochMilli() == TIMESTAMP_1
        trades.get(1).entryOrderStatus == FILLED
        !trades.get(1).exitOrderType
        !trades.get(1).exitOrderAmount
        !trades.get(1).exitOrderPrice
        !trades.get(1).exitOrderTimestamp
        !trades.get(1).exitOrderStatus
    }

    private mockSuccessfulSeriesRetrievalCall(String symbol, Interval interval, String responseBody) {
        wireMockServer.stubFor(get(urlEqualTo(seriesRetrievalUrl(symbol, interval)))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(responseBody)
                .withHeader('Connection', 'close')))
    }

    private mockFailedSeriesRetrievalCall(String symbol, Interval interval) {
        wireMockServer.stubFor(get(urlEqualTo(seriesRetrievalUrl(symbol, interval)))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader('Connection', 'close')))
    }

    private String seriesRetrievalUrl(String symbol, Interval interval) {
        def intervalRepresentation = exchangeGatewayConfigurationProperties.getIntervalRepresentation(interval)
        "/klines?symbol=$symbol&interval=$intervalRepresentation&limit=$SERIES_LENGTH"
    }

    private mockSuccessfulOrderExecutionCall(String requestBody, String responseBody) {
        wireMockServer.stubFor(post(urlPathEqualTo('/order'))
            .withHeader('Content-Type', equalTo('application/x-www-form-urlencoded'))
            .withHeader('X-MBX-APIKEY', equalTo(exchangeGatewayConfigurationProperties.getApiKey()))
            .withRequestBody(equalTo(requestBody))
            .willReturn(aResponse()
                .withStatus(200)
                .withBody(responseBody)
                .withHeader('Connection', 'close')))
    }

    private mockFailedOrderExecutionCall(String requestBody) {
        wireMockServer.stubFor(post(urlPathEqualTo('/order'))
            .withHeader('Content-Type', equalTo('application/x-www-form-urlencoded'))
            .withHeader('X-MBX-APIKEY', equalTo(exchangeGatewayConfigurationProperties.getApiKey()))
            .withRequestBody(equalTo(requestBody))
            .willReturn(aResponse()
                .withStatus(500)
                .withHeader('Connection', 'close')))
    }

    static class SampleStrategy implements Strategy {

        private final ExchangeGateway exchangeGateway

        SampleStrategy(ExchangeGateway exchangeGateway) {
            this.exchangeGateway = exchangeGateway
        }

        @Override
        String getId() { 'strategy-1' }

        @Override
        String getName() { 'strategy-1' }

        @Override
        TradeType getTradeType() { LONG }

        @Override
        EntryRule getEntryRule() {
            new EntryRule() {
                @Override
                RuleCheckResult check(String symbol) {
                    exchangeGateway.getSeries(symbol, FIVE_MINUTES, SERIES_LENGTH)
                        .map({series ->
                            def barOnWhichRuleWasSatisfied = series.getLastBar()
                            RuleCheckResult.satisfied(barOnWhichRuleWasSatisfied)
                        })
                        .orElseGet({ RuleCheckResult.notSatisfied() })
                }
                @Override
                Interval getHighestInterval() { FIVE_MINUTES }
            }
        }

        @Override
        ExitRule getExitRule() {
            new ExitRule() {
                @Override
                RuleCheckResult check(Order entryOrder) {
                    def symbol = entryOrder.getSymbol()
                    exchangeGateway.getSeries(symbol, ONE_MINUTE, SERIES_LENGTH)
                        .map({series ->
                            def barOnWhichRuleWasSatisfied = series.getLastBar()
                            RuleCheckResult.satisfied(barOnWhichRuleWasSatisfied)
                        })
                        .orElseGet({ RuleCheckResult.notSatisfied() })
                }
                @Override
                Interval getLowestInterval() { ONE_MINUTE }
            }
        }
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        StrategyExecution strategyExecution(ExchangeGateway exchangeGateway,
                                            StrategyExecutionFactory strategyExecutionFactory) {
            def strategy = new SampleStrategy(exchangeGateway)
            def strategyExecutionContext = StrategyExecutionContext.builder()
                .strategy(strategy)
                .symbols(SYMBOLS)
                .executionMode(TRADING)
                .allowedNumberOfSimultaneouslyOpenedTrades(NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES)
                .fundsPerTrade(FUNDS_PER_TRADE)
                .build()
            def strategyExecution = strategyExecutionFactory.create(strategyExecutionContext)
            strategyExecution.start()
            strategyExecution
        }

        @Bean
        WireMockServer wireMockServer() {
            def wireMockServer = new WireMockServer(wireMockConfig().port(7777))
            wireMockServer.start()
            wireMockServer
        }
    }
}
