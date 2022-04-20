package root.tse.domain.order_execution

import root.tse.domain.ExchangeGateway
import root.tse.domain.IdGenerator
import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.event.EventBus
import spock.lang.Specification

import static root.tse.util.TestUtils.ORDER_EXECUTION_ID

class OrderExecutionFactoryTest extends Specification {

    private orderExecutionContext = OrderExecutionContext.builder().build()
    private idGenerator = Mock(IdGenerator)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private exchangeGateway = Mock(ExchangeGateway)
    private eventBus = Mock(EventBus)

    private orderExecutionFactory = new OrderExecutionFactory(idGenerator, clockSignalDispatcher, exchangeGateway, eventBus)

    def 'should create order execution correctly'() {
        when:
        def orderExecution = orderExecutionFactory.create(orderExecutionContext)

        then:
        1 * idGenerator.generate() >> ORDER_EXECUTION_ID
        0 * _

        and:
        orderExecution.id == ORDER_EXECUTION_ID
        orderExecution.context == orderExecutionContext
        orderExecution.clockSignalDispatcher == clockSignalDispatcher
        orderExecution.exchangeGateway == exchangeGateway
        orderExecution.eventBus == eventBus
        orderExecution.executedOrder.isEmpty()
    }
}
