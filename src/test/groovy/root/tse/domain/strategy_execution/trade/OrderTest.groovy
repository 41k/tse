package root.tse.domain.strategy_execution.trade

import spock.lang.Specification

import static root.tse.domain.strategy_execution.trade.OrderStatus.*

class OrderTest extends Specification {

    def 'should provide status'() {
        when:
        def order = Order.builder().build()

        then:
        order.getStatus() == NEW
        order.wasNotFilled()

        when:
        order = order.toBuilder().status(NOT_FILLED).build()

        then:
        order.wasNotFilled()

        when:
        order = order.toBuilder().status(FILLED).build()

        then:
        !order.wasNotFilled()
    }

    def 'should provide total'() {
        given:
        def order = Order.builder().amount(0.08d).price(3865.71d).build()

        expect:
        order.getTotal() == 309.2568d
    }
}
