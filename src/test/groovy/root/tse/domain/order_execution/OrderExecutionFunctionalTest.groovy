package root.tse.domain.order_execution

import org.springframework.beans.factory.annotation.Autowired
import root.tse.BaseFunctionalTest
import root.tse.domain.clock.Interval
import root.tse.domain.rule.EntryRule

import static root.tse.domain.order.OrderExecutionType.MARKET
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.*

class OrderExecutionFunctionalTest extends BaseFunctionalTest {

    private static final RULE_INTERVAL = Interval.ONE_HOUR

    @Autowired
    private OrderExecutionFactory orderExecutionFactory

    private OrderExecution orderExecution

    def setup() {
        exchangeGatewayMock().reset()
        exchangeGatewayMock().orderFeePercent = ORDER_FEE_PERCENT
        exchangeGatewayMock().currentPrices = [(SYMBOL_1): [(BUY): PRICE_1, (SELL): PRICE_2]]
        exchangeGatewayMock().orderExecutionSuccess = true
        def orderExecutionContext = OrderExecutionContext.builder()
            .rule(rule()).orderType(BUY).orderExecutionType(MARKET).symbol(SYMBOL_1).amount(AMOUNT_1).build()
        orderExecution = orderExecutionFactory.create(orderExecutionContext)
        orderExecution.start()
    }

    def 'should execute order successfully and stop order execution'() {
        given: 'order has not been executed'
        assert orderExecution.executedOrder.isEmpty()

        when: 'clock signal for rule'
        clockSignalDispatcher.dispatch(clockSignal(RULE_INTERVAL, TIMESTAMP_1))

        then: 'order has been executed'
        orderExecution.executedOrder.get() == ENTRY_ORDER

        when: 'another clock signals for rule'
        clockSignalDispatcher.dispatch(clockSignal(RULE_INTERVAL, TIMESTAMP_2))

        then: 'no other order was executed and previously executed order is still in place'
        orderExecution.executedOrder.get() == ENTRY_ORDER
    }

    def 'should not execute order if clock signal required by rule was not dispatched'() {
        given: 'order has not been executed'
        assert orderExecution.executedOrder.isEmpty()

        when: 'clock signal which is not required for rule'
        clockSignalDispatcher.dispatch(clockSignal(Interval.ONE_DAY, TIMESTAMP_1))

        then: 'order has not been executed yet'
        orderExecution.executedOrder.isEmpty()
    }

    def 'should not change state if order execution attempt failed'() {
        given: 'order has not been executed'
        assert orderExecution.executedOrder.isEmpty()

        and: 'entry order execution failed'
        exchangeGatewayMock().orderExecutionSuccess = false

        when: 'clock signal for rule'
        clockSignalDispatcher.dispatch(clockSignal(RULE_INTERVAL, TIMESTAMP_1))

        then: 'order has not been executed yet'
        orderExecution.executedOrder.isEmpty()
    }

    private EntryRule rule() {
        new EntryRule() {
            @Override
            Interval getCheckInterval() { RULE_INTERVAL }
            @Override
            boolean isSatisfied(String symbol) { true }
        }
    }
}
