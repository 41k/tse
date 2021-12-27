package root.tse.util

import org.apache.commons.math3.util.Precision
import org.ta4j.core.Bar
import root.tse.application.model.ChainExchangeExecutionSettings
import root.tse.application.model.command.StartChainExchangeExecutionCommand
import root.tse.application.model.command.StopChainExchangeExecutionCommand
import root.tse.configuration.properties.ExchangeGatewayConfigurationProperties.SymbolSettings
import root.tse.domain.chain_exchange_execution.ChainExchange
import root.tse.domain.chain_exchange_execution.ChainExchangeExecutionContext
import root.tse.domain.clock.ClockSignal
import root.tse.domain.clock.Interval
import root.tse.domain.order.Order
import root.tse.domain.order.OrderExecutionMode
import root.tse.domain.strategy_execution.Strategy
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.trade.Trade
import root.tse.domain.strategy_execution.trade.TradeClosingContext
import root.tse.domain.strategy_execution.trade.TradeOpeningContext
import root.tse.domain.strategy_execution.trade.TradeType
import root.tse.infrastructure.persistence.chain_exchange.ChainExchangeDbEntry
import root.tse.infrastructure.persistence.trade.TradeDbEntry

import java.nio.charset.StandardCharsets
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

import static root.tse.domain.order.OrderStatus.FILLED
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG

class TestUtils {

    public static final TRADE_ID = '34598437'
    public static final STRATEGY_ID = '5def5d38'
    public static final STRATEGY_NAME = 'strategy-name-1'
    public static final STRATEGY_EXECUTION_ID = '3545de05'
    public static final ORDER_EXECUTION_MODE = OrderExecutionMode.EXCHANGE_GATEWAY
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
    public static final ORDER_FEE_PERCENT = 0.2d

    public static final ENTRY_ORDER = Order.builder()
        .status(FILLED).type(BUY).symbol(SYMBOL_1).amount(AMOUNT_1).price(PRICE_1).timestamp(TIMESTAMP_1).build()

    public static final EXIT_ORDER = Order.builder()
        .type(SELL).symbol(SYMBOL_1).amount(AMOUNT_2).price(PRICE_2).timestamp(TIMESTAMP_2).build()

    public static final OPENED_TRADE = Trade.builder()
        .id(TRADE_ID).strategyExecutionId(STRATEGY_EXECUTION_ID).type(LONG)
        .orderFeePercent(ORDER_FEE_PERCENT).entryOrderClockSignal(CLOCK_SIGNAL_1)
        .entryOrder(ENTRY_ORDER).build()

    public static final CLOSED_TRADE = OPENED_TRADE.toBuilder()
        .exitOrder(EXIT_ORDER.toBuilder().status(FILLED).build()).build()

    public static final OPENED_TRADE_DB_ENTRY = TradeDbEntry.builder()
        .id(TRADE_ID).strategyExecutionId(STRATEGY_EXECUTION_ID).type(LONG).symbol(SYMBOL_1)
        .orderFeePercent(ORDER_FEE_PERCENT).entryOrderStatus(FILLED).entryOrderType(BUY)
        .entryOrderAmount(AMOUNT_1).entryOrderPrice(PRICE_1).entryOrderTimestamp(Instant.ofEpochMilli(TIMESTAMP_1))
        .build()

    public static final CLOSED_TRADE_DB_ENTRY = OPENED_TRADE_DB_ENTRY.toBuilder()
        .exitOrderStatus(FILLED).exitOrderType(SELL).exitOrderAmount(AMOUNT_2)
        .exitOrderPrice(PRICE_2).exitOrderTimestamp(Instant.ofEpochMilli(TIMESTAMP_2))
        .build()


    public static final CHAIN_EXCHANGE_ID = '0ffe45d1'
    public static final CHAIN_EXCHANGE_AMOUNT = 1000d
    public static final ASSET_1_CODE = 'ASSET1'
    public static final ASSET_2_CODE = 'ASSET2'
    public static final ASSET_3_CODE = 'ASSET3'
    public static final ASSET_CODE_DELIMITER = '/'
    public static final ASSET_CHAIN_ID = 1
    public static final ASSET_CHAIN = [ASSET_1_CODE, ASSET_2_CODE, ASSET_3_CODE, ASSET_1_CODE]
    public static final ASSET_CHAINS = [(ASSET_CHAIN_ID) : ASSET_CHAIN]
    public static final ASSET_CHAIN_AS_STRING = "$ASSET_1_CODE$ASSET_CODE_DELIMITER$ASSET_2_CODE$ASSET_CODE_DELIMITER$ASSET_3_CODE$ASSET_CODE_DELIMITER$ASSET_1_CODE"
    public static final CHAIN_SYMBOL_1 = "$ASSET_2_CODE$ASSET_CODE_DELIMITER$ASSET_1_CODE" as String
    public static final CHAIN_SYMBOL_2 = "$ASSET_2_CODE$ASSET_CODE_DELIMITER$ASSET_3_CODE" as String
    public static final CHAIN_SYMBOL_3 = "$ASSET_3_CODE$ASSET_CODE_DELIMITER$ASSET_1_CODE" as String
    public static final CHAIN_SYMBOLS = [CHAIN_SYMBOL_1, CHAIN_SYMBOL_2, CHAIN_SYMBOL_3]
    public static final CHAIN_SYMBOL_1_PRECISION = 3
    public static final CHAIN_SYMBOL_2_PRECISION = 5
    public static final CHAIN_SYMBOL_3_PRECISION = 4
    public static final CHAIN_ORDER_1_AMOUNT = 0.331d
    public static final CHAIN_ORDER_2_AMOUNT = 0.331d
    public static final CHAIN_ORDER_3_AMOUNT = 0.0228d
    public static final CHAIN_SYMBOL_1_BUY_PRICE = 2885.18d
    public static final CHAIN_SYMBOL_1_SELL_PRICE = 2885.08d
    public static final CHAIN_SYMBOL_2_BUY_PRICE = 0.06904d
    public static final CHAIN_SYMBOL_2_SELL_PRICE = 0.06903d
    public static final CHAIN_SYMBOL_3_BUY_PRICE = 42505d
    public static final CHAIN_SYMBOL_3_SELL_PRICE = 42500d
    public static final CHAIN_PRICES = [
        (CHAIN_SYMBOL_1) : [(BUY) : CHAIN_SYMBOL_1_BUY_PRICE, (SELL) : CHAIN_SYMBOL_1_SELL_PRICE],
        (CHAIN_SYMBOL_2) : [(BUY) : CHAIN_SYMBOL_2_BUY_PRICE, (SELL) : CHAIN_SYMBOL_2_SELL_PRICE],
        (CHAIN_SYMBOL_3) : [(BUY) : CHAIN_SYMBOL_3_BUY_PRICE, (SELL) : CHAIN_SYMBOL_3_SELL_PRICE]
    ]
    public static final SYMBOL_SETTINGS = [
        'symbol-1' : new SymbolSettings(name: CHAIN_SYMBOL_1, precision: CHAIN_SYMBOL_1_PRECISION),
        'symbol-2' : new SymbolSettings(name: CHAIN_SYMBOL_2, precision: CHAIN_SYMBOL_2_PRECISION),
        'symbol-3' : new SymbolSettings(name: CHAIN_SYMBOL_3, precision: CHAIN_SYMBOL_3_PRECISION)
    ]
    public static final SYMBOL_TO_PRECISION_MAP = [
        (CHAIN_SYMBOL_1) : CHAIN_SYMBOL_1_PRECISION,
        (CHAIN_SYMBOL_2) : CHAIN_SYMBOL_2_PRECISION,
        (CHAIN_SYMBOL_3) : CHAIN_SYMBOL_3_PRECISION
    ]
    public static final MIN_PROFIT_THRESHOLD = 10d
    public static final N_AMOUNT_SELECTION_STEPS = 20
    public static final CHAIN_EXCHANGE_EXECUTION_SETTINGS = ChainExchangeExecutionSettings.builder()
        .assetChains(ASSET_CHAINS).assetCodeDelimiter(ASSET_CODE_DELIMITER).symbolToPrecisionMap(SYMBOL_TO_PRECISION_MAP)
        .orderFeePercent(ORDER_FEE_PERCENT).nAmountSelectionSteps(N_AMOUNT_SELECTION_STEPS).build()
    public static final CHAIN_EXCHANGE_EXECUTION_CONTEXT = ChainExchangeExecutionContext.builder()
        .assetChain(ASSET_CHAIN).assetCodeDelimiter(ASSET_CODE_DELIMITER).amount(CHAIN_EXCHANGE_AMOUNT)
        .symbolToPrecisionMap(SYMBOL_TO_PRECISION_MAP).orderFeePercent(ORDER_FEE_PERCENT)
        .minProfitThreshold(MIN_PROFIT_THRESHOLD).orderExecutionMode(ORDER_EXECUTION_MODE)
        .nAmountSelectionSteps(N_AMOUNT_SELECTION_STEPS).build()
    public static final CHAIN_ORDER_1 = Order.builder().type(BUY)
        .symbol(CHAIN_SYMBOL_1).amount(CHAIN_ORDER_1_AMOUNT).price(CHAIN_SYMBOL_1_BUY_PRICE).build()
    public static final CHAIN_ORDER_2 = Order.builder().type(SELL)
        .symbol(CHAIN_SYMBOL_2).amount(CHAIN_ORDER_2_AMOUNT).price(CHAIN_SYMBOL_2_SELL_PRICE).build()
    public static final CHAIN_ORDER_3 = Order.builder().type(SELL)
        .symbol(CHAIN_SYMBOL_3).amount(CHAIN_ORDER_3_AMOUNT).price(CHAIN_SYMBOL_3_SELL_PRICE).build()
    public static final CHAIN_EXCHANGE_PROFIT = 10.15743083999996d
    public static final CHAIN_EXCHANGE_EXECUTION_TIMESTAMP = 1643284983000L
    public static final EXPECTED_CHAIN_EXCHANGE = ChainExchange.builder()
        .id(CHAIN_EXCHANGE_ID)
        .assetChain(ASSET_CHAIN_AS_STRING)
        .orderFeePercent(ORDER_FEE_PERCENT)
        .order1(CHAIN_ORDER_1)
        .order2(CHAIN_ORDER_2)
        .order3(CHAIN_ORDER_3)
        .profit(CHAIN_EXCHANGE_PROFIT)
        .timestamp(CHAIN_EXCHANGE_EXECUTION_TIMESTAMP)
        .build()
    public static final EXECUTED_CHAIN_EXCHANGE = EXPECTED_CHAIN_EXCHANGE.toBuilder()
        .order1(CHAIN_ORDER_1.toBuilder().status(FILLED).build())
        .order2(CHAIN_ORDER_2.toBuilder().status(FILLED).build())
        .order3(CHAIN_ORDER_3.toBuilder().status(FILLED).build())
        .build()
    public static final CHAIN_EXCHANGE_DB_ENTRY = ChainExchangeDbEntry.builder()
        .id(CHAIN_EXCHANGE_ID)
        .assetChain(ASSET_CHAIN_AS_STRING)
        .orderFeePercent(ORDER_FEE_PERCENT)
        .executionTimestamp(Instant.ofEpochMilli(CHAIN_EXCHANGE_EXECUTION_TIMESTAMP))
        .order1Type(BUY)
        .order1Symbol(CHAIN_SYMBOL_1)
        .order1Amount(CHAIN_ORDER_1_AMOUNT)
        .order1Price(CHAIN_SYMBOL_1_BUY_PRICE)
        .order2Type(SELL)
        .order2Symbol(CHAIN_SYMBOL_2)
        .order2Amount(CHAIN_ORDER_2_AMOUNT)
        .order2Price(CHAIN_SYMBOL_2_SELL_PRICE)
        .order3Type(SELL)
        .order3Symbol(CHAIN_SYMBOL_3)
        .order3Amount(CHAIN_ORDER_3_AMOUNT)
        .order3Price(CHAIN_SYMBOL_3_SELL_PRICE)
        .profit(CHAIN_EXCHANGE_PROFIT)
        .build()
    public static final START_CHAIN_EXCHANGE_EXECUTION_COMMAND = StartChainExchangeExecutionCommand.builder()
        .assetChainId(ASSET_CHAIN_ID).amount(CHAIN_EXCHANGE_AMOUNT).minProfitThreshold(MIN_PROFIT_THRESHOLD)
        .orderExecutionMode(ORDER_EXECUTION_MODE).build()
    public static final STOP_CHAIN_EXCHANGE_EXECUTION_COMMAND = new StopChainExchangeExecutionCommand(ASSET_CHAIN_ID)


    static ClockSignal createClockSignal(Interval clockSignalInterval = Interval.ONE_MINUTE, Long timestamp = TIMESTAMP_1) {
        new ClockSignal(clockSignalInterval, timestamp)
    }

    static TradeOpeningContext createTradeOpeningContext(Bar bar) {
        TradeOpeningContext.builder()
            .strategyExecutionId(STRATEGY_EXECUTION_ID)
            .orderExecutionMode(ORDER_EXECUTION_MODE)
            .tradeType(LONG)
            .orderFeePercent(ORDER_FEE_PERCENT)
            .entryOrderClockSignal(CLOCK_SIGNAL_1)
            .symbol(SYMBOL_1)
            .bar(bar)
            .fundsPerTrade(FUNDS_PER_TRADE)
            .build()
    }

    static TradeClosingContext createTradeClosingContext(Bar bar) {
        TradeClosingContext.builder()
            .orderExecutionMode(ORDER_EXECUTION_MODE)
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

    static String applyUrlEncoding(String string) {
        URLEncoder.encode(string, StandardCharsets.UTF_8.toString())
    }
}
