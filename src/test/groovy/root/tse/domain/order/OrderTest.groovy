package root.tse.domain.order

import spock.lang.Specification
import spock.lang.Unroll

import static root.tse.domain.order.OrderStatus.*
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL

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

    @Unroll
    def 'should provide net total for #orderType order'() {
        given:
        def order = Order.builder().type(orderType).amount(0.08d).price(3865.71d).build()
        def orderFeePercent = 0.2d

        expect:
        order.getNetTotal(orderFeePercent) == result

        where:
        orderType || result
        BUY       || 309.8753136d
        SELL      || 308.63828639999997d
    }
}
