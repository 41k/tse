package root.tse.domain.strategy_execution.event

import spock.lang.Specification

import static root.tse.util.TestData.*

class StrategyExecutionEventBusTest extends Specification {

    private eventSubscriber1 = Mock(StrategyExecutionEventSubscriber)
    private eventSubscriber2 = Mock(StrategyExecutionEventSubscriber)
    private eventBus = new StrategyExecutionEventBus([eventSubscriber1, eventSubscriber2])

    def 'should dispatch events correctly'() {
        when:
        eventBus.publishTradeWasOpenedEvent(OPENED_TRADE)

        then:
        1 * eventSubscriber1.acceptTradeWasOpenedEvent(OPENED_TRADE)
        1 * eventSubscriber2.acceptTradeWasOpenedEvent(OPENED_TRADE)
        0 * _

        when:
        eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON)

        then:
        1 * eventSubscriber1.acceptTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON)
        1 * eventSubscriber2.acceptTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON)
        0 * _

        when:
        eventBus.publishTradeWasClosedEvent(CLOSED_TRADE)

        then:
        1 * eventSubscriber1.acceptTradeWasClosedEvent(CLOSED_TRADE)
        1 * eventSubscriber2.acceptTradeWasClosedEvent(CLOSED_TRADE)
        0 * _

        when:
        eventBus.publishTradeWasNotClosedEvent(TRADE_TO_CLOSE, REASON)

        then:
        1 * eventSubscriber1.acceptTradeWasNotClosedEvent(TRADE_TO_CLOSE, REASON)
        1 * eventSubscriber2.acceptTradeWasNotClosedEvent(TRADE_TO_CLOSE, REASON)
        0 * _
    }
}
