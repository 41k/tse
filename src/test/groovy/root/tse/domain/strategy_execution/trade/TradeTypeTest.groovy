package root.tse.domain.strategy_execution.trade

import root.tse.domain.order.OrderType
import spock.lang.Specification

class TradeTypeTest extends Specification {

    def 'should provide correct order type'() {
        expect:
        TradeType.LONG.getEntryOrderType() == OrderType.BUY
        TradeType.LONG.getExitOrderType() == OrderType.SELL

        and:
        TradeType.SHORT.getEntryOrderType() == OrderType.SELL
        TradeType.SHORT.getExitOrderType() == OrderType.BUY
    }
}
