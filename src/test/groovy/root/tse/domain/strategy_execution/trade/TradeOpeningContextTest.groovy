package root.tse.domain.strategy_execution.trade

import org.ta4j.core.Bar
import org.ta4j.core.num.PrecisionNum
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import static root.tse.domain.strategy_execution.trade.OrderStatus.NEW
import static root.tse.domain.strategy_execution.trade.OrderType.BUY
import static root.tse.util.TestUtils.*

class TradeOpeningContextTest extends Specification {

    def 'should provide correct entry order'() {
        given:
        def bar = Mock(Bar)
        def context = createTradeOpeningContext(bar)

        when:
        def entryOrder = context.getEntryOrder()

        then:
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(PRICE_1)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_1), ZoneId.systemDefault())
        0 * _

        and:
        entryOrder.status == NEW
        entryOrder.type == BUY
        entryOrder.symbol == SYMBOL_1
        entryOrder.amount == AMOUNT_1
        entryOrder.price == PRICE_1
        entryOrder.timestamp == TIMESTAMP_1
    }
}
