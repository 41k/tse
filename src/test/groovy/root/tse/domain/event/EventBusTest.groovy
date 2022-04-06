package root.tse.domain.event

import spock.lang.Specification

import static root.tse.domain.order.OrderType.BUY
import static root.tse.util.TestUtils.*

class EventBusTest extends Specification {

    private eventSubscriber1 = Mock(EventSubscriber)
    private eventSubscriber2 = Mock(EventSubscriber)
    private eventBus = new EventBus([eventSubscriber1, eventSubscriber2])

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

    def 'should dispatch CHAIN EXCHANGE WAS EXECUTED event'() {
        when:
        eventBus.publishChainExchangeWasExecutedEvent(EXECUTED_CHAIN_EXCHANGE)

        then:
        1 * eventSubscriber1.acceptChainExchangeWasExecutedEvent(EXECUTED_CHAIN_EXCHANGE)
        1 * eventSubscriber2.acceptChainExchangeWasExecutedEvent(EXECUTED_CHAIN_EXCHANGE)
        0 * _

        when:
        eventBus.publishChainExchangeWasExecutedEvent(EXECUTED_CHAIN_EXCHANGE)

        then:
        1 * eventSubscriber1.acceptChainExchangeWasExecutedEvent(EXECUTED_CHAIN_EXCHANGE)
        1 * eventSubscriber2.acceptChainExchangeWasExecutedEvent(EXECUTED_CHAIN_EXCHANGE) >> { throw new RuntimeException() }
        0 * _

        and:
        noExceptionThrown()
    }

    def 'should dispatch CHAIN EXCHANGE EXECUTION FAILED event'() {
        when:
        eventBus.publishChainExchangeExecutionFailedEvent(CHAIN_EXCHANGE_ID, ASSET_CHAIN_AS_STRING)

        then:
        1 * eventSubscriber1.acceptChainExchangeExecutionFailedEvent(CHAIN_EXCHANGE_ID, ASSET_CHAIN_AS_STRING)
        1 * eventSubscriber2.acceptChainExchangeExecutionFailedEvent(CHAIN_EXCHANGE_ID, ASSET_CHAIN_AS_STRING)
        0 * _

        when:
        eventBus.publishChainExchangeExecutionFailedEvent(CHAIN_EXCHANGE_ID, ASSET_CHAIN_AS_STRING)

        then:
        1 * eventSubscriber1.acceptChainExchangeExecutionFailedEvent(CHAIN_EXCHANGE_ID, ASSET_CHAIN_AS_STRING)
        1 * eventSubscriber2.acceptChainExchangeExecutionFailedEvent(CHAIN_EXCHANGE_ID, ASSET_CHAIN_AS_STRING) >> { throw new RuntimeException() }
        0 * _

        and:
        noExceptionThrown()
    }

    def 'should dispatch ORDER WAS EXECUTED event'() {
        when:
        eventBus.publishOrderWasExecutedEvent(ORDER_EXECUTION_ID, ENTRY_ORDER)

        then:
        1 * eventSubscriber1.acceptOrderWasExecutedEvent(ORDER_EXECUTION_ID, ENTRY_ORDER)
        1 * eventSubscriber2.acceptOrderWasExecutedEvent(ORDER_EXECUTION_ID, ENTRY_ORDER)
        0 * _

        when:
        eventBus.publishOrderWasExecutedEvent(ORDER_EXECUTION_ID, ENTRY_ORDER)

        then:
        1 * eventSubscriber1.acceptOrderWasExecutedEvent(ORDER_EXECUTION_ID, ENTRY_ORDER)
        1 * eventSubscriber2.acceptOrderWasExecutedEvent(ORDER_EXECUTION_ID, ENTRY_ORDER) >> { throw new RuntimeException() }
        0 * _

        and:
        noExceptionThrown()
    }

    def 'should dispatch ORDER EXECUTION FAILED event'() {
        when:
        eventBus.publishOrderExecutionFailedEvent(ORDER_EXECUTION_ID, BUY, SYMBOL_1)

        then:
        1 * eventSubscriber1.acceptOrderExecutionFailedEvent(ORDER_EXECUTION_ID, BUY, SYMBOL_1)
        1 * eventSubscriber2.acceptOrderExecutionFailedEvent(ORDER_EXECUTION_ID, BUY, SYMBOL_1)
        0 * _

        when:
        eventBus.publishOrderExecutionFailedEvent(ORDER_EXECUTION_ID, BUY, SYMBOL_1)

        then:
        1 * eventSubscriber1.acceptOrderExecutionFailedEvent(ORDER_EXECUTION_ID, BUY, SYMBOL_1)
        1 * eventSubscriber2.acceptOrderExecutionFailedEvent(ORDER_EXECUTION_ID, BUY, SYMBOL_1) >> { throw new RuntimeException() }
        0 * _

        and:
        noExceptionThrown()
    }
}
