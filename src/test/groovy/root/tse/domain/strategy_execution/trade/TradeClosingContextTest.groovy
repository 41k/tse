package root.tse.domain.strategy_execution.trade

import org.ta4j.core.Bar
import org.ta4j.core.num.PrecisionNum
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import static root.tse.domain.strategy_execution.trade.OrderStatus.NEW
import static root.tse.domain.strategy_execution.trade.OrderType.SELL
import static root.tse.util.TestUtils.*

class TradeClosingContextTest extends Specification {

    def 'should provide correct exit order'() {
        given:
        def bar = Mock(Bar)
        def context = createTradeClosingContext(bar)

        when:
        def exitOrder = context.getExitOrder()

        then:
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(PRICE_2)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_2), ZoneId.systemDefault())
        0 * _

        and:
        exitOrder.status == NEW
        exitOrder.type == SELL
        exitOrder.symbol == SYMBOL_1
        exitOrder.amount == AMOUNT_1
        exitOrder.price == PRICE_2
        exitOrder.timestamp == TIMESTAMP_2
    }
}
