package root.tse.domain.strategy_execution.event

import spock.lang.Specification

import static root.tse.util.TestUtils.*

class StrategyExecutionEventBusTest extends Specification {

    private eventSubscriber1 = Mock(StrategyExecutionEventSubscriber)
    private eventSubscriber2 = Mock(StrategyExecutionEventSubscriber)
    private eventBus = new StrategyExecutionEventBus([eventSubscriber1, eventSubscriber2])

    def 'should dispatch TRADE WAS OPENED event'() {
        when:
        eventBus.publishTradeWasOpenedEvent(OPENED_TRADE)

        then:
        1 * eventSubscriber1.acceptTradeWasOpenedEvent(OPENED_TRADE)
        1 * eventSubscriber2.acceptTradeWasOpenedEvent(OPENED_TRADE)
        0 * _

        when:
        eventBus.publishTradeWasOpenedEvent(OPENED_TRADE)

        then:
        1 * eventSubscriber1.acceptTradeWasOpenedEvent(OPENED_TRADE) >> { throw new RuntimeException() }
        1 * eventSubscriber2.acceptTradeWasOpenedEvent(OPENED_TRADE)
        0 * _

        and:
        noExceptionThrown()
    }

    def 'should dispatch TRADE WAS NOT OPENED event'() {
        when:
        eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON)

        then:
        1 * eventSubscriber1.acceptTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON)
        1 * eventSubscriber2.acceptTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON)
        0 * _

        when:
        eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON)

        then:
        1 * eventSubscriber1.acceptTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON)
        1 * eventSubscriber2.acceptTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, REASON) >> { throw new RuntimeException() }
        0 * _

        and:
        noExceptionThrown()
    }

    def 'should dispatch TRADE WAS CLOSED event'() {
        when:
        eventBus.publishTradeWasClosedEvent(CLOSED_TRADE)

        then:
        1 * eventSubscriber1.acceptTradeWasClosedEvent(CLOSED_TRADE)
        1 * eventSubscriber2.acceptTradeWasClosedEvent(CLOSED_TRADE)
        0 * _

        when:
        eventBus.publishTradeWasClosedEvent(CLOSED_TRADE)

        then:
        1 * eventSubscriber1.acceptTradeWasClosedEvent(CLOSED_TRADE) >> { throw new RuntimeException() }
        1 * eventSubscriber2.acceptTradeWasClosedEvent(CLOSED_TRADE)
        0 * _

        and:
        noExceptionThrown()
    }

    def 'should dispatch TRADE WAS NOT CLOSED event'() {
        when:
        eventBus.publishTradeWasNotClosedEvent(OPENED_TRADE, REASON)

        then:
        1 * eventSubscriber1.acceptTradeWasNotClosedEvent(OPENED_TRADE, REASON)
        1 * eventSubscriber2.acceptTradeWasNotClosedEvent(OPENED_TRADE, REASON)
        0 * _

        when:
        eventBus.publishTradeWasNotClosedEvent(OPENED_TRADE, REASON)

        then:
        1 * eventSubscriber1.acceptTradeWasNotClosedEvent(OPENED_TRADE, REASON)
        1 * eventSubscriber2.acceptTradeWasNotClosedEvent(OPENED_TRADE, REASON) >> { throw new RuntimeException() }
        0 * _

        and:
        noExceptionThrown()
    }
}
