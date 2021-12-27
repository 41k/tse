package root.tse.domain.strategy_execution.trade

import org.ta4j.core.Bar
import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.MarketScanningStrategyExecution
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.rule.RuleCheckResult
import spock.lang.Specification

import static root.tse.domain.clock.Interval.ONE_MINUTE
import static root.tse.util.TestUtils.*

class TradeExecutionTest extends Specification {

    private bar = Mock(Bar)
    private exitRule = Mock(ExitRule)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private strategyExecution = Mock(MarketScanningStrategyExecution)

    private tradeExecution = new TradeExecution(OPENED_TRADE, exitRule, clockSignalDispatcher, strategyExecution)

    def 'trade execution id should be trade id'() {
        expect:
        tradeExecution.getId() == TRADE_ID
    }

    def 'should be started correctly'() {
        when:
        tradeExecution.start()

        then: 'subscribe to clock signals with the lowest interval of exit rule'
        1 * exitRule.getLowestInterval() >> ONE_MINUTE
        1 * clockSignalDispatcher.subscribe([ONE_MINUTE] as Set, tradeExecution)
        0 * _
    }

    def 'should be stopped correctly'() {
        when:
        tradeExecution.stop()

        then: 'unsubscribe from clock signals with the lowest interval of exit rule'
        1 * exitRule.getLowestInterval() >> ONE_MINUTE
        1 * clockSignalDispatcher.unsubscribe([ONE_MINUTE] as Set, tradeExecution)
        0 * _
    }

    def 'should close trade if exit rule is satisfied'() {
        when:
        tradeExecution.accept(CLOCK_SIGNAL_2)

        then: 'check if exit rule is satisfied'
        1 * exitRule.check(CLOCK_SIGNAL_2, ENTRY_ORDER) >> RuleCheckResult.satisfied(bar)

        and: 'close trade'
        1 * strategyExecution.closeTrade(OPENED_TRADE, bar)

        and: 'no other actions'
        0 * _
    }

    def 'should not close trade if exit rule is not satisfied'() {
        when:
        tradeExecution.accept(CLOCK_SIGNAL_2)

        then: 'check if exit rule is satisfied'
        1 * exitRule.check(CLOCK_SIGNAL_2, ENTRY_ORDER) >> RuleCheckResult.notSatisfied()

        and: 'no other actions'
        0 * _
    }

    def 'should not close trade if clock signal has timestamp which is similar to entry order clock signal timestamp'() {
        when:
        tradeExecution.accept(CLOCK_SIGNAL_2_WITH_TIMESTAMP_OF_CLOCK_SIGNAL_1)

        then: 'no other actions'
        0 * _
    }
}
