package root.tse.domain.strategy_execution.trade

import root.tse.domain.order.Order
import root.tse.domain.order.OrderType
import spock.lang.Specification
import spock.lang.Unroll

import static root.tse.util.TestUtils.*

class TradeTest extends Specification {

    def 'should provide symbol and amount of entry order'() {
        given:
        def openedTrade = tradeBuilder().build()

        expect:
        openedTrade.getSymbol() == SYMBOL_1
        openedTrade.getAmount() == AMOUNT_1
    }

    @Unroll
    def 'should provide correct exit order type'() {
        given:
        def trade = tradeBuilder().type(tradeType).build()

        expect:
        trade.getExitOrderType() == exitOrderType

        where:
        tradeType       || exitOrderType
        TradeType.LONG  || OrderType.SELL
        TradeType.SHORT || OrderType.BUY
    }

    def 'should tell if it is closed'() {
        expect:
        CLOSED_TRADE.isClosed()
        !OPENED_TRADE.isClosed()
    }

    def 'should calculate profit successfully for closed LONG trade'() {
        given:
        def amount = 0.05d
        def trade = tradeBuilder().type(TradeType.LONG)
            .orderFeePercent(0.2d)
            .entryOrder(Order.builder().type(OrderType.BUY).amount(amount).price(3187.15d).build())
            .exitOrder(Order.builder().type(OrderType.SELL).amount(amount).price(3243.21d).build())
            .build()

        expect:
        trade.getProfit() == 2.159964000000002d
    }

    def 'should calculate profit successfully for opened LONG trade'() {
        given:
        def trade = tradeBuilder().type(TradeType.LONG)
            .orderFeePercent(0.2d)
            .entryOrder(Order.builder().type(OrderType.BUY).amount(0.1d).price(3500.7d).build())
            .build()

        expect:
        trade.getProfit() == -350.77013999999997d
    }

    def 'should throw exception during profit calculation for SHORT trade'() {
        given:
        def trade = tradeBuilder().type(TradeType.SHORT).build()

        when:
        trade.getProfit()

        then:
        def exception = thrown(UnsupportedOperationException)
        exception.message == 'Profit calculation for SHORT trade has not been implemented yet.'
    }

    private Trade.TradeBuilder tradeBuilder() {
        Trade.builder().id(TRADE_ID).strategyExecutionId(STRATEGY_EXECUTION_ID)
            .type(TradeType.LONG).orderFeePercent(ORDER_FEE_PERCENT)
            .entryOrder(Order.builder().symbol(SYMBOL_1).amount(AMOUNT_1).build())
    }
}
