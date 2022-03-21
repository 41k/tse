package root.tse.domain.strategy_execution

import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.clock.Interval
import root.tse.domain.event.EventBus
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.trade.TradeService
import spock.lang.Specification

import static org.apache.commons.lang3.StringUtils.EMPTY
import static root.tse.domain.strategy_execution.trade.TradeType.LONG
import static root.tse.util.TestUtils.*

class SimpleStrategyExecutionTest extends Specification {

    private entryRule = Mock(EntryRule)
    private exitRule = Mock(ExitRule)
    private strategyExecutionContext = StrategyExecutionContext.builder()
        .entryRule(entryRule).exitRule(exitRule).tradeType(LONG).symbols([SYMBOL_1])
        .orderExecutionType(ORDER_EXECUTION_TYPE).fundsPerTrade(FUNDS_PER_TRADE).build()
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeService = Mock(TradeService)
    private eventBus = Mock(EventBus)

    private SimpleStrategyExecution strategyExecution

    def setup() {
        strategyExecution = new SimpleStrategyExecution(
            STRATEGY_EXECUTION_ID, strategyExecutionContext, clockSignalDispatcher, tradeService, eventBus)
    }

    def 'should provide id'() {
        expect:
        strategyExecution.getId() == STRATEGY_EXECUTION_ID
    }

    def 'should be started correctly'() {
        when:
        strategyExecution.start()

        then:
        1 * entryRule.getCheckInterval() >> Interval.ONE_MINUTE
        1 * exitRule.getCheckInterval() >> Interval.FIFTEEN_MINUTES
        1 * clockSignalDispatcher.subscribe([Interval.ONE_MINUTE, Interval.FIFTEEN_MINUTES] as Set, strategyExecution)
        0 * _
    }

    def 'should be stopped correctly'() {
        when:
        strategyExecution.stop()

        then:
        1 * entryRule.getCheckInterval() >> Interval.ONE_HOUR
        1 * exitRule.getCheckInterval() >> Interval.FIVE_MINUTES
        1 * clockSignalDispatcher.unsubscribe([Interval.ONE_HOUR, Interval.FIVE_MINUTES] as Set, strategyExecution)
        0 * _
    }

    def 'should open trade successfully'() {
        given: 'no trade has been opened yet'
        assert !strategyExecution.openedTrade

        when:
        strategyExecution.accept(CLOCK_SIGNAL_1)

        then: 'check entry rule: satisfied'
        1 * entryRule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1) >> true

        and: 'try to open trade: trade was opened'
        1 * tradeService.tryToOpenTrade(TRADE_OPENING_CONTEXT) >> Optional.of(OPENED_TRADE)

        and: 'publish correct event'
        1 * eventBus.publishTradeWasOpenedEvent(OPENED_TRADE)

        and: 'no other actions'
        0 * _

        and:
        strategyExecution.openedTrade == OPENED_TRADE
    }

    def 'should not open trade if entry rule was not satisfied'() {
        given: 'no trade has been opened yet'
        assert !strategyExecution.openedTrade

        when:
        strategyExecution.accept(CLOCK_SIGNAL_1)

        then: 'check entry rule: not satisfied'
        1 * entryRule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1) >> false

        and: 'no other actions'
        0 * _

        and: 'no trade has been opened yet'
        !strategyExecution.openedTrade
    }

    def 'should not change state if trade opening attempt failed'() {
        given: 'no trade has been opened yet'
        assert !strategyExecution.openedTrade

        when:
        strategyExecution.accept(CLOCK_SIGNAL_1)

        then: 'check entry rule: satisfied'
        1 * entryRule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1) >> true

        and: 'try to open trade: trade was not opened'
        1 * tradeService.tryToOpenTrade(TRADE_OPENING_CONTEXT) >> Optional.empty()

        and: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, EMPTY)

        and: 'no other actions'
        0 * _

        and: 'no trade has been opened yet'
        !strategyExecution.openedTrade
    }

    def 'should close trade successfully'() {
        given: 'opened trade'
        strategyExecution.openedTrade = OPENED_TRADE

        when:
        strategyExecution.accept(CLOCK_SIGNAL_2)

        then: 'check exit rule: satisfied'
        1 * exitRule.isSatisfied(CLOCK_SIGNAL_2, ENTRY_ORDER) >> true

        and: 'try to close trade: trade was closed'
        1 * tradeService.tryToCloseTrade(OPENED_TRADE, CLOCK_SIGNAL_2) >> Optional.of(CLOSED_TRADE)

        and: 'publish correct event'
        1 * eventBus.publishTradeWasClosedEvent(CLOSED_TRADE)

        and: 'no other actions'
        0 * _

        and: 'trade has been closed'
        !strategyExecution.openedTrade
    }

    def 'should not close trade if exit rule was not satisfied'() {
        given: 'opened trade'
        strategyExecution.openedTrade = OPENED_TRADE

        when:
        strategyExecution.accept(CLOCK_SIGNAL_2)

        then: 'check exit rule: not satisfied'
        1 * exitRule.isSatisfied(CLOCK_SIGNAL_2, ENTRY_ORDER) >> false

        and: 'no other actions'
        0 * _

        and: 'trade is still opened'
        strategyExecution.openedTrade == OPENED_TRADE
    }

    def 'should not change state if trade closing attempt failed'() {
        given: 'opened trade'
        strategyExecution.openedTrade = OPENED_TRADE

        when:
        strategyExecution.accept(CLOCK_SIGNAL_2)

        then: 'check exit rule: satisfied'
        1 * exitRule.isSatisfied(CLOCK_SIGNAL_2, ENTRY_ORDER) >> true

        and: 'try to close trade: trade was not closed'
        1 * tradeService.tryToCloseTrade(OPENED_TRADE, CLOCK_SIGNAL_2) >> Optional.empty()

        and: 'publish correct event'
        1 * eventBus.publishTradeWasNotClosedEvent(OPENED_TRADE, EMPTY)

        and: 'no other actions'
        0 * _

        and: 'trade is still opened'
        strategyExecution.openedTrade == OPENED_TRADE
    }
}
