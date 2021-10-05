package root.tse.domain.strategy_execution.trade

import root.tse.domain.strategy_execution.ExchangeGateway
import root.tse.domain.strategy_execution.StrategyExecutionMode
import spock.lang.Specification

import static root.tse.domain.strategy_execution.trade.OrderStatus.*

class OrderExecutorTest extends Specification {

    private exchangeGateway = Mock(ExchangeGateway)
    private orderExecutor = new OrderExecutor(exchangeGateway)

    def 'should execute order successfully'() {
        given:
        def orderToExecute = Order.builder().build()

        and:
        assert orderToExecute.status == NEW

        when:
        def executedOrder = orderExecutor.execute(orderToExecute, StrategyExecutionMode.TRADING)

        then:
        1 * exchangeGateway.execute(orderToExecute) >> {
            return orderToExecute.toBuilder().status(FILLED).build()
        }
        0 * _

        and:
        executedOrder.status == FILLED
    }

    def 'should not execute order in INCUBATION strategy execution mode but return FILLED order'() {
        given:
        def orderToExecute = Order.builder().build()

        and:
        assert orderToExecute.status == NEW

        when:
        def executedOrder = orderExecutor.execute(orderToExecute, StrategyExecutionMode.INCUBATION)

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
        def executedOrder = orderExecutor.execute(orderToExecute, StrategyExecutionMode.TRADING)

        then:
        1 * exchangeGateway.execute(orderToExecute) >> {
            return orderToExecute.toBuilder().status(NOT_FILLED).build()
        }
        0 * _

        and:
        executedOrder.status == NOT_FILLED
    }
}
