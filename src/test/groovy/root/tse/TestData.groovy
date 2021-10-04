package root.tse

import root.tse.domain.strategy_execution.trade.Order
import root.tse.domain.strategy_execution.trade.Trade
import root.tse.infrastructure.persistence.trade.TradeDbEntry

import java.time.Instant

import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED
import static root.tse.domain.strategy_execution.trade.OrderStatus.NOT_FILLED
import static root.tse.domain.strategy_execution.trade.OrderType.BUY
import static root.tse.domain.strategy_execution.trade.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG

class TestData {

    public static final TRADE_ID = 'trade-1'
    public static final STRATEGY_EXECUTION_ID = 'strategy-execution-1'
    public static final SYMBOL_1 = 'symbol-1'
    public static final SYMBOL_2 = 'symbol-2'
    public static final SYMBOL_3 = 'symbol-3'
    public static final AMOUNT_1 = 1.5d
    public static final AMOUNT_2 = 1.45d
    public static final PRICE_1 = 2000.0d
    public static final PRICE_2 = 2100.0d
    public static final TIMESTAMP_1 = 1633222800000L
    public static final TIMESTAMP_2 = 1633309200000L

    public static final TRADE = Trade.builder().id(TRADE_ID).strategyExecutionId(STRATEGY_EXECUTION_ID).type(LONG)
        .entryOrder(Order.builder().status(FILLED).type(BUY).symbol(SYMBOL_1).amount(AMOUNT_1)
            .price(PRICE_1).timestamp(TIMESTAMP_1).build())
        .exitOrder(Order.builder().status(NOT_FILLED).type(SELL).symbol(SYMBOL_1).amount(AMOUNT_2)
            .price(PRICE_2).timestamp(TIMESTAMP_2).build())
        .build()

    public static final TRADE_DB_ENTRY = TradeDbEntry.builder()
        .id(TRADE_ID).strategyExecutionId(STRATEGY_EXECUTION_ID).type(LONG).symbol(SYMBOL_1)
        .entryOrderStatus(FILLED).entryOrderType(BUY).entryOrderAmount(AMOUNT_1)
        .entryOrderPrice(PRICE_1).entryOrderTimestamp(Instant.ofEpochMilli(TIMESTAMP_1))
        .exitOrderStatus(NOT_FILLED).exitOrderType(SELL).exitOrderAmount(AMOUNT_2)
        .exitOrderPrice(PRICE_2).exitOrderTimestamp(Instant.ofEpochMilli(TIMESTAMP_2))
        .build()
}
