package root.tse.domain.strategy_execution

import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import root.tse.BaseFunctionalTest
import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.clock.Interval
import root.tse.domain.clock.SequentialClockSignalDispatcher
import root.tse.domain.order.Order
import root.tse.domain.rule.EntryRule
import root.tse.domain.rule.ExitRule

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

import static root.tse.util.TestUtils.clockSignal

@ContextConfiguration(classes = [TestContextConfiguration])
abstract class BaseStrategyExecutionFunctionalTest extends BaseFunctionalTest {

    protected static final SYMBOL_1 = 'symbol-1'
    protected static final SYMBOL_2 = 'symbol-2'
    protected static final SYMBOLS = [SYMBOL_1, SYMBOL_2]
    protected static final AMOUNT_1 = 20d
    protected static final AMOUNT_2 = 10d
    protected static final PRICE_1 = 10.0d
    protected static final PRICE_2 = 20.0d
    protected static final FUNDS_PER_TRADE = 200d
    protected static final ORDER_FEE_PERCENT = 0.2d
    protected static final NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES = 2
    protected static final ENTRY_RULE_CLOCK_SIGNAL_INTERVAL = Interval.FIVE_MINUTES
    protected static final ENTRY_RULE_CLOCK_SIGNAL_TIMESTAMP = 1647324900000L
    protected static final ENTRY_RULE_CLOCK_SIGNAL = clockSignal(ENTRY_RULE_CLOCK_SIGNAL_INTERVAL, ENTRY_RULE_CLOCK_SIGNAL_TIMESTAMP)
    protected static final EXIT_RULE_CLOCK_SIGNAL_INTERVAL = Interval.ONE_MINUTE
    protected static final EXIT_RULE_CLOCK_SIGNAL_TIMESTAMP = 1647325620000L
    protected static final EXIT_RULE_CLOCK_SIGNAL = clockSignal(EXIT_RULE_CLOCK_SIGNAL_INTERVAL, EXIT_RULE_CLOCK_SIGNAL_TIMESTAMP)
    protected static final EXIT_RULE_CLOCK_SIGNAL_WITH_ENTRY_ORDER_TIMESTAMP = clockSignal(EXIT_RULE_CLOCK_SIGNAL_INTERVAL, ENTRY_RULE_CLOCK_SIGNAL_TIMESTAMP)
    protected static final NOT_REQUIRED_CLOCK_SIGNAL = clockSignal(Interval.FIFTEEN_MINUTES, 1647325800000L)
    protected static final CLOCK_TIMESTAMP = ENTRY_RULE_CLOCK_SIGNAL_TIMESTAMP

    protected EntryRule entryRule() {
        new EntryRule() {
            @Override
            Interval getCheckInterval() { ENTRY_RULE_CLOCK_SIGNAL_INTERVAL }
            @Override
            boolean isSatisfied(String symbol) { true }
        }
    }

    protected ExitRule exitRule() {
        new ExitRule() {
            @Override
            Interval getCheckInterval() { EXIT_RULE_CLOCK_SIGNAL_INTERVAL }
            @Override
            boolean isSatisfied(Order entryOrder) { true }
        }
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        Clock clock() { Clock.fixed(Instant.ofEpochMilli(CLOCK_TIMESTAMP), ZoneId.systemDefault()) }
        @Bean
        ClockSignalDispatcher clockSignalDispatcher() { new SequentialClockSignalDispatcher() }
    }
}
