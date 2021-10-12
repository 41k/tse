package root.tse.domain.strategy_execution.trade

import org.ta4j.core.Bar
import org.ta4j.core.num.PrecisionNum
import root.tse.domain.strategy_execution.Interval
import root.tse.domain.strategy_execution.StrategyExecution
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.rule.RuleCheckResult
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import static root.tse.domain.strategy_execution.trade.OrderStatus.NEW
import static root.tse.domain.strategy_execution.trade.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG
import static root.tse.util.TestData.*

class TradeExecutionTest extends Specification {

    private bar = Mock(Bar)
    private exitRule = Mock(ExitRule)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private strategyExecution = Mock(StrategyExecution)

    private tradeExecution = new TradeExecution(OPENED_TRADE, exitRule, clockSignalDispatcher, strategyExecution)

    def 'trade execution id should be trade id'() {
        expect:
        tradeExecution.getId() == TRADE_ID
    }

    def 'should be started correctly'() {
        when:
        tradeExecution.start()

        then: 'subscribe to clock signals with the lowest interval of exit rule'
        1 * exitRule.getLowestInterval() >> Interval.ONE_MINUTE
        1 * clockSignalDispatcher.subscribe(Interval.ONE_MINUTE, tradeExecution)
        0 * _
    }

    def 'should be stopped correctly'() {
        when:
        tradeExecution.stop()

        then: 'unsubscribe from clock signals with the lowest interval of exit rule'
        1 * exitRule.getLowestInterval() >> Interval.ONE_MINUTE
        1 * clockSignalDispatcher.unsubscribe(Interval.ONE_MINUTE, tradeExecution)
        0 * _
    }

    def 'should close trade if exit rule is satisfied'() {
        given:
        def ruleCheckResult = RuleCheckResult.satisfied(bar)

        when:
        tradeExecution.acceptClockSignal()

        then: 'check if exit rule is satisfied'
        1 * exitRule.check(ENTRY_ORDER) >> ruleCheckResult

        and: 'close trade'
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(PRICE_2)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_2), ZoneId.systemDefault())
        1 * strategyExecution.closeTrade(_) >> {
            def tradeToClose = it[0] as Trade
            assert tradeToClose.id == TRADE_ID
            assert tradeToClose.strategyExecutionId == STRATEGY_EXECUTION_ID
            assert tradeToClose.type == LONG
            assert tradeToClose.entryOrder == ENTRY_ORDER
            assert tradeToClose.exitOrder.status == NEW
            assert tradeToClose.exitOrder.type == SELL
            assert tradeToClose.exitOrder.symbol == SYMBOL_1
            assert tradeToClose.exitOrder.amount == AMOUNT_1
            assert tradeToClose.exitOrder.price == PRICE_2
            assert tradeToClose.exitOrder.timestamp == TIMESTAMP_2
        }

        and: 'no other actions'
        0 * _
    }

    def 'should not close trade if exit rule is not satisfied'() {
        given:
        def ruleCheckResult = RuleCheckResult.notSatisfied()

        when:
        tradeExecution.acceptClockSignal()

        then: 'check if exit rule is satisfied'
        1 * exitRule.check(ENTRY_ORDER) >> ruleCheckResult

        and: 'no other actions'
        0 * _
    }
}
