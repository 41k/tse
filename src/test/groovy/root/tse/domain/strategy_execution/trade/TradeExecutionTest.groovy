package root.tse.domain.strategy_execution.trade

import org.ta4j.core.Bar
import org.ta4j.core.num.PrecisionNum
import root.tse.domain.strategy_execution.StrategyExecution
import root.tse.domain.strategy_execution.Interval
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
import static root.tse.domain.strategy_execution.rule.RuleCheckStatus.SATISFIED
import static root.tse.domain.strategy_execution.rule.RuleCheckStatus.NOT_SATISFIED

class TradeExecutionTest extends Specification {

    private static final SYMBOL = 'ETHUSD'
    private static final INTERVAL = Interval.ONE_MINUTE
    private static final AMOUNT = 2.5d
    private static final TRADE_ID = 'TRADE-1'
    private static final STRATEGY_EXECUTION_ID = 'STRATEGY-EXECUTION-1'
    private static final ENTRY_ORDER = Order.builder().symbol(SYMBOL).amount(AMOUNT).build()
    private static final OPENED_TRADE_TO_EXECUTE =
        Trade.builder().id(TRADE_ID).strategyExecutionId(STRATEGY_EXECUTION_ID).type(LONG).entryOrder(ENTRY_ORDER).build()
    private static final LAST_BAR_CLOSE_PRICE = 2000d
    private static final LAST_BAR_TIMESTAMP = 1598532602000L

    private bar = Mock(Bar)
    private exitRule = Mock(ExitRule)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private strategyExecution = Mock(StrategyExecution)

    private tradeExecution = new TradeExecution(
        OPENED_TRADE_TO_EXECUTE, exitRule, clockSignalDispatcher, strategyExecution)

    def 'trade execution id should be trade id'() {
        expect:
        tradeExecution.getId() == TRADE_ID
    }

    def 'should be started correctly'() {
        when:
        tradeExecution.start()

        then: 'subscribe to clock signals with the lowest interval of exit rule'
        1 * exitRule.getLowestInterval() >> INTERVAL
        1 * clockSignalDispatcher.subscribe(INTERVAL, tradeExecution)
        0 * _
    }

    def 'should be stopped correctly'() {
        when:
        tradeExecution.stop()

        then: 'unsubscribe from clock signals with the lowest interval of exit rule'
        1 * exitRule.getLowestInterval() >> INTERVAL
        1 * clockSignalDispatcher.unsubscribe(INTERVAL, tradeExecution)
        0 * _
    }

    def 'should close trade if exit rule is satisfied'() {
        given:
        def ruleCheckResult = RuleCheckResult.builder().status(SATISFIED).barOnWhichRuleWasSatisfied(bar).build()

        when:
        tradeExecution.acceptClockSignal()

        then: 'check if exit rule is satisfied'
        1 * exitRule.check(ENTRY_ORDER) >> ruleCheckResult

        and: 'close trade'
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(LAST_BAR_CLOSE_PRICE)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(LAST_BAR_TIMESTAMP), ZoneId.systemDefault())
        1 * strategyExecution.closeTrade(_) >> {
            def tradeToClose = it[0] as Trade
            assert tradeToClose.id == TRADE_ID
            assert tradeToClose.strategyExecutionId == STRATEGY_EXECUTION_ID
            assert tradeToClose.type == LONG
            assert tradeToClose.entryOrder == ENTRY_ORDER
            assert tradeToClose.exitOrder.status == NEW
            assert tradeToClose.exitOrder.type == SELL
            assert tradeToClose.exitOrder.symbol == SYMBOL
            assert tradeToClose.exitOrder.amount == AMOUNT
            assert tradeToClose.exitOrder.price == LAST_BAR_CLOSE_PRICE
            assert tradeToClose.exitOrder.timestamp == LAST_BAR_TIMESTAMP
        }

        and: 'no other actions'
        0 * _
    }

    def 'should not close trade if exit rule is not satisfied'() {
        given:
        def ruleCheckResult = RuleCheckResult.builder().status(NOT_SATISFIED).build()

        when:
        tradeExecution.acceptClockSignal()

        then: 'check if exit rule is satisfied'
        1 * exitRule.check(ENTRY_ORDER) >> ruleCheckResult

        and: 'no other actions'
        0 * _
    }
}
