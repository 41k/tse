package root.tse.domain.order_execution

import root.tse.domain.ExchangeGateway
import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.clock.Interval
import root.tse.domain.event.EventBus
import root.tse.domain.order.Order
import root.tse.domain.rule.EntryRule
import spock.lang.Specification

import static root.tse.domain.order.OrderType.BUY
import static root.tse.util.TestUtils.*

class OrderExecutionTest extends Specification {

    private rule = Mock(EntryRule)
    private orderExecutionContext = OrderExecutionContext.builder()
        .rule(rule).orderType(BUY).orderExecutionType(ORDER_EXECUTION_TYPE).symbol(SYMBOL_1).amount(AMOUNT_1).build()
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private exchangeGateway = Mock(ExchangeGateway)
    private eventBus = Mock(EventBus)

    private OrderExecution orderExecution

    def setup() {
        orderExecution = new OrderExecution(
            ORDER_EXECUTION_ID, orderExecutionContext, clockSignalDispatcher, exchangeGateway, eventBus)
    }

    def 'should provide id'() {
        expect:
        orderExecution.getId() == ORDER_EXECUTION_ID
    }

    def 'should provide context'() {
        expect:
        orderExecution.getContext() == orderExecutionContext
    }

    def 'should be started correctly'() {
        when:
        orderExecution.start()

        then:
        1 * rule.getCheckInterval() >> Interval.ONE_MINUTE
        1 * clockSignalDispatcher.subscribe([Interval.ONE_MINUTE] as Set, orderExecution)
        0 * _
    }

    def 'should be stopped correctly'() {
        when:
        orderExecution.stop()

        then:
        1 * rule.getCheckInterval() >> Interval.ONE_HOUR
        1 * clockSignalDispatcher.unsubscribe([Interval.ONE_HOUR] as Set, orderExecution)
        0 * _
    }

    def 'should execute order successfully'() {
        given: 'order has not been executed'
        assert orderExecution.executedOrder.isEmpty()

        when:
        orderExecution.accept(CLOCK_SIGNAL_1)

        then: 'check rule: satisfied'
        1 * rule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1) >> true

        and: 'execute order'
        1 * exchangeGateway.tryToExecute(_) >> {
            def order = it[0] as Order
            assertOrderBeforeExecution(order)
            return Optional.of(order.toBuilder().price(PRICE_1).build())
        }

        and: 'stop order execution'
        1 * rule.getCheckInterval() >> Interval.ONE_SECOND
        1 * clockSignalDispatcher.unsubscribe([Interval.ONE_SECOND] as Set, orderExecution)

        and: 'publish correct event'
        1 * eventBus.publishOrderWasExecutedEvent(ORDER_EXECUTION_ID, ENTRY_ORDER)

        and: 'no other actions'
        0 * _

        and:
        orderExecution.executedOrder.get() == ENTRY_ORDER
    }

    def 'should skip clock signals if order has been executed'() {
        given: 'already executed order'
        orderExecution.executedOrder = Optional.of(ENTRY_ORDER)

        when:
        orderExecution.accept(CLOCK_SIGNAL_1)
        orderExecution.accept(CLOCK_SIGNAL_1)
        orderExecution.accept(CLOCK_SIGNAL_1)

        then: 'no actions'
        0 * _

        and:
        orderExecution.executedOrder.get() == ENTRY_ORDER
    }

    def 'should not execute order if rule was not satisfied'() {
        given: 'order has not been executed'
        assert orderExecution.executedOrder.isEmpty()

        when:
        orderExecution.accept(CLOCK_SIGNAL_1)

        then: 'check rule: not satisfied'
        1 * rule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1) >> false

        and: 'no other actions'
        0 * _

        and: 'order has not been executed yet'
        orderExecution.executedOrder.isEmpty()
    }

    def 'should not change state if order execution attempt failed'() {
        given: 'order has not been executed'
        assert orderExecution.executedOrder.isEmpty()

        when:
        orderExecution.accept(CLOCK_SIGNAL_1)

        then: 'check rule: satisfied'
        1 * rule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1) >> true

        and: 'failed order execution'
        1 * exchangeGateway.tryToExecute(_) >> {
            def order = it[0] as Order
            assertOrderBeforeExecution(order)
            return Optional.empty()
        }

        and: 'publish correct event'
        1 * eventBus.publishOrderExecutionFailedEvent(ORDER_EXECUTION_ID, BUY, SYMBOL_1)

        and: 'no other actions'
        0 * _

        and: 'order has not been executed yet'
        orderExecution.executedOrder.isEmpty()
    }

    private assertOrderBeforeExecution(Order entryOrder) {
        assert entryOrder.type == BUY
        assert entryOrder.executionType == ORDER_EXECUTION_TYPE
        assert entryOrder.symbol == SYMBOL_1
        assert entryOrder.amount == AMOUNT_1
        assert !entryOrder.price
        assert entryOrder.timestamp == TIMESTAMP_1
    }
}
