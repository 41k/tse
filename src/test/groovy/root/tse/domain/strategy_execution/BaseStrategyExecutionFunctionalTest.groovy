package root.tse.domain.strategy_execution

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import root.tse.BaseFunctionalTest
import root.tse.domain.ExchangeGateway
import root.tse.domain.clock.Interval
import root.tse.domain.order.Order
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.rule.RuleCheckResult
import root.tse.domain.strategy_execution.trade.TradeType

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

import static com.google.common.io.Resources.getResource
import static root.tse.domain.strategy_execution.trade.TradeType.LONG
import static root.tse.util.TestUtils.createClockSignal

@ContextConfiguration(classes = [TestContextConfiguration])
abstract class BaseStrategyExecutionFunctionalTest extends BaseFunctionalTest {

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
    protected static final ORDER_FEE_PERCENT = 0.2d
    protected static final NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES = 2
    protected static final ENTRY_RULE_CLOCK_SIGNAL_INTERVAL = Interval.FIVE_MINUTES
    protected static final ENTRY_RULE_CLOCK_SIGNAL = createClockSignal(ENTRY_RULE_CLOCK_SIGNAL_INTERVAL, TIMESTAMP_1)
    protected static final EXIT_RULE_CLOCK_SIGNAL_INTERVAL = Interval.ONE_MINUTE
    protected static final EXIT_RULE_CLOCK_SIGNAL = createClockSignal(EXIT_RULE_CLOCK_SIGNAL_INTERVAL, TIMESTAMP_2)
    protected static final EXIT_RULE_CLOCK_SIGNAL_WITH_ENTRY_ORDER_CLOCK_SIGNAL_TIMESTAMP = createClockSignal(EXIT_RULE_CLOCK_SIGNAL_INTERVAL, TIMESTAMP_1)
    protected static final NOT_REQUIRED_CLOCK_SIGNAL = createClockSignal(Interval.FIFTEEN_MINUTES, TIMESTAMP_2)

    protected static final SERIES_RETRIEVAL_RESPONSE_TEMPLATE = getResource('json/series-retrieval-response.json').text

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
                        .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                        .replace('$SIGNATURE', 'f024eb3af789a3f58442464b8d8bf7ffec49639c859e6e9899b2391917fd8ffb'),
        (SYMBOL_2) : ORDER_EXECUTION_REQUEST_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_2 as String)
                        .replace('$ORDER_TYPE', 'SELL')
                        .replace('$SYMBOL', SYMBOL_2)
                        .replace('$TIMESTAMP', TIMESTAMP_1 as String)
                        .replace('$SIGNATURE', '54bfb3a7cefda9294e3e0f224367dd98f6a334956eba6d5d0a307b3db9929521')
    ]
    private static final EXIT_ORDER_EXECUTION_RESPONSE = [
        (SYMBOL_1) : ORDER_EXECUTION_RESPONSE_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_1 as String)
                        .replace('$PRICE', PRICE_2 as String),
        (SYMBOL_2) : ORDER_EXECUTION_RESPONSE_TEMPLATE
                        .replace('$AMOUNT', AMOUNT_2 as String)
                        .replace('$PRICE', PRICE_1 as String)
    ]

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

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        Clock clock() {
            Clock.fixed(Instant.ofEpochMilli(TIMESTAMP_1), ZoneId.systemDefault())
        }
    }
}
