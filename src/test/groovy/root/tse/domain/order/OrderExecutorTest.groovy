package root.tse.domain.order

import root.tse.domain.ExchangeGateway
import spock.lang.Specification

import static root.tse.domain.order.OrderStatus.*

class OrderExecutorTest extends Specification {

    private exchangeGateway = Mock(ExchangeGateway)
    private orderExecutor = new OrderExecutor(exchangeGateway)

    def 'should execute order successfully in EXCHANGE_GATEWAY execution mode'() {
        given:
        def orderToExecute = Order.builder().build()

        and:
        assert orderToExecute.status == NEW

        when:
        def executedOrder = orderExecutor.execute(orderToExecute, OrderExecutionMode.EXCHANGE_GATEWAY)

        then:
        1 * exchangeGateway.execute(orderToExecute) >> {
            return orderToExecute.toBuilder().status(FILLED).build()
        }
        0 * _

        and:
        executedOrder.status == FILLED
    }

    def 'should not execute order in STUB execution mode but return FILLED order'() {
        given:
        def orderToExecute = Order.builder().build()

        and:
        assert orderToExecute.status == NEW

        when:
        def executedOrder = orderExecutor.execute(orderToExecute, OrderExecutionMode.STUB)

        then:
        0 * _

        and:
        executedOrder.status == FILLED
    }

    def 'should return NOT_FILLED order if exchange gateway failed to fill the order'() {
        given:
        def orderToExecute = Order.builder().build()

        and:
        assert orderToExecute.status == NEW

        when:
        def executedOrder = orderExecutor.execute(orderToExecute, OrderExecutionMode.EXCHANGE_GATEWAY)

        then:
        1 * exchangeGateway.execute(orderToExecute) >> {
            return orderToExecute.toBuilder().status(NOT_FILLED).build()
        }
        0 * _

        and:
        executedOrder.status == NOT_FILLED
    }
}
