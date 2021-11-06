package root.tse.util

import org.apache.commons.math3.util.Precision
import org.ta4j.core.Bar
import root.tse.domain.strategy_execution.Interval
import root.tse.domain.strategy_execution.Strategy
import root.tse.domain.strategy_execution.StrategyExecutionMode
import root.tse.domain.strategy_execution.clock.ClockSignal
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.trade.Order
import root.tse.domain.strategy_execution.trade.Trade
import root.tse.domain.strategy_execution.trade.TradeClosingContext
import root.tse.domain.strategy_execution.trade.TradeOpeningContext
import root.tse.domain.strategy_execution.trade.TradeType
import root.tse.infrastructure.persistence.trade.TradeDbEntry

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED
import static root.tse.domain.strategy_execution.trade.OrderType.BUY
import static root.tse.domain.strategy_execution.trade.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG

class TestUtils {

    public static final TRADE_ID = 'trade-1'
    public static final STRATEGY_ID = 'strategy-1'
    public static final STRATEGY_NAME = 'strategy-name-1'
    public static final STRATEGY_EXECUTION_ID = 'strategy-execution-1'
    public static final STRATEGY_EXECUTION_MODE = StrategyExecutionMode.TRADING
    public static final SYMBOL_1 = 'symbol-1'
    public static final SYMBOL_2 = 'symbol-2'
    public static final SYMBOL_3 = 'symbol-3'
    public static final SYMBOLS = [SYMBOL_1, SYMBOL_2, SYMBOL_3]
    public static final AMOUNT_1 = 0.1d
    public static final AMOUNT_2 = 1d
    public static final PRICE_1 = 2000.0d
    public static final PRICE_2 = 2100.0d
    public static final TIMESTAMP_1 = 1633222800000L
    public static final TIMESTAMP_2 = 1633309200000L
    public static final FUNDS_PER_TRADE = 200d
    public static final NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES = 3
    public static final CLOCK = Clock.fixed(Instant.ofEpochMilli(TIMESTAMP_1), ZoneId.systemDefault())
    public static final CLOCK_SIGNAL_1 = createClockSignal(Interval.ONE_MINUTE, TIMESTAMP_1)
    public static final CLOCK_SIGNAL_2 = createClockSignal(Interval.ONE_MINUTE, TIMESTAMP_2)
    public static final CLOCK_SIGNAL_2_WITH_TIMESTAMP_OF_CLOCK_SIGNAL_1 = createClockSignal(Interval.ONE_MINUTE, TIMESTAMP_1)
    public static final REASON = 'reason-1'
    public static final DATA_SET_NAME = 'data-set-1'
    public static final TRANSACTION_FEE_PERCENT = 0.2d

    public static final ENTRY_ORDER = Order.builder()
        .status(FILLED).type(BUY).symbol(SYMBOL_1).amount(AMOUNT_1).price(PRICE_1).timestamp(TIMESTAMP_1).build()

    public static final EXIT_ORDER = Order.builder()
        .type(SELL).symbol(SYMBOL_1).amount(AMOUNT_2).price(PRICE_2).timestamp(TIMESTAMP_2).build()

    public static final OPENED_TRADE = Trade.builder()
        .id(TRADE_ID).strategyExecutionId(STRATEGY_EXECUTION_ID).type(LONG)
        .transactionFeePercent(TRANSACTION_FEE_PERCENT).entryOrderClockSignal(CLOCK_SIGNAL_1)
        .entryOrder(ENTRY_ORDER).build()

    public static final CLOSED_TRADE = OPENED_TRADE.toBuilder()
        .exitOrder(EXIT_ORDER.toBuilder().status(FILLED).build()).build()

    public static final OPENED_TRADE_DB_ENTRY = TradeDbEntry.builder()
        .id(TRADE_ID).strategyExecutionId(STRATEGY_EXECUTION_ID).type(LONG).symbol(SYMBOL_1)
        .transactionFeePercent(TRANSACTION_FEE_PERCENT).entryOrderStatus(FILLED).entryOrderType(BUY)
        .entryOrderAmount(AMOUNT_1).entryOrderPrice(PRICE_1).entryOrderTimestamp(Instant.ofEpochMilli(TIMESTAMP_1))
        .build()

    public static final CLOSED_TRADE_DB_ENTRY = OPENED_TRADE_DB_ENTRY.toBuilder()
        .exitOrderStatus(FILLED).exitOrderType(SELL).exitOrderAmount(AMOUNT_2)
        .exitOrderPrice(PRICE_2).exitOrderTimestamp(Instant.ofEpochMilli(TIMESTAMP_2))
        .build()

    static ClockSignal createClockSignal(Interval clockSignalInterval = Interval.ONE_MINUTE, Long timestamp = TIMESTAMP_1) {
        new ClockSignal(clockSignalInterval, timestamp)
    }

    static TradeOpeningContext createTradeOpeningContext(Bar bar) {
        TradeOpeningContext.builder()
            .strategyExecutionId(STRATEGY_EXECUTION_ID)
            .strategyExecutionMode(STRATEGY_EXECUTION_MODE)
            .tradeType(LONG)
            .transactionFeePercent(TRANSACTION_FEE_PERCENT)
            .entryOrderClockSignal(CLOCK_SIGNAL_1)
            .symbol(SYMBOL_1)
            .bar(bar)
            .fundsPerTrade(FUNDS_PER_TRADE)
            .build()
    }

    static TradeClosingContext createTradeClosingContext(Bar bar) {
        TradeClosingContext.builder()
            .strategyExecutionMode(STRATEGY_EXECUTION_MODE)
            .openedTrade(OPENED_TRADE)
            .bar(bar)
            .build()
    }

    static Strategy createStrategy(EntryRule entryRule, ExitRule exitRule) {
        new Strategy() {
            @Override
            String getId() {
                return STRATEGY_ID
            }
            @Override
            String getName() {
                return STRATEGY_NAME
            }
            @Override
            TradeType getTradeType() {
                return LONG
            }
            @Override
            EntryRule getEntryRule() {
                return entryRule
            }
            @Override
            ExitRule getExitRule() {
                return exitRule
            }
        }
    }

    static boolean equalsWithPrecision(Number actualValue, double expectedValue, int precision) {
        def actualValueWithPrecision = Precision.round(actualValue as double, precision)
        actualValueWithPrecision == expectedValue
    }
}
