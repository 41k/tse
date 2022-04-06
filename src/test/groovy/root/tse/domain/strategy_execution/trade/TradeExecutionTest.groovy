package root.tse.domain.strategy_execution.trade

import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.MarketScanningStrategyExecution
import root.tse.domain.rule.ExitRule
import spock.lang.Specification

import static root.tse.domain.clock.Interval.ONE_MINUTE
import static root.tse.util.TestUtils.*

class TradeExecutionTest extends Specification {

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
        1 * exitRule.getCheckInterval() >> ONE_MINUTE
        1 * clockSignalDispatcher.subscribe([ONE_MINUTE] as Set, tradeExecution)
        0 * _
    }

    def 'should be stopped correctly'() {
        when:
        tradeExecution.stop()

        then: 'unsubscribe from clock signals with the lowest interval of exit rule'
        1 * exitRule.getCheckInterval() >> ONE_MINUTE
        1 * clockSignalDispatcher.unsubscribe([ONE_MINUTE] as Set, tradeExecution)
        0 * _
    }

    def 'should close trade if exit rule is satisfied'() {
        when:
        tradeExecution.accept(CLOCK_SIGNAL_2)

        then: 'check exit rule: satisfied'
        1 * exitRule.isSatisfied(CLOCK_SIGNAL_2, ENTRY_ORDER) >> true

        and: 'close trade'
        1 * strategyExecution.closeTrade(OPENED_TRADE, CLOCK_SIGNAL_2)

        and: 'no other actions'
        0 * _
    }

    def 'should not close trade if exit rule is not satisfied'() {
        when:
        tradeExecution.accept(CLOCK_SIGNAL_2)

        then: 'check exit rule: not satisfied'
        1 * exitRule.isSatisfied(CLOCK_SIGNAL_2, ENTRY_ORDER) >> false

        and: 'no other actions'
        0 * _
    }
}
