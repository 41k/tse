package root.tse.infrastructure.persistence.trade;

import root.tse.domain.order.Order;
import root.tse.domain.strategy_execution.trade.Trade;

import java.time.Instant;
import java.util.Optional;

import static java.util.Objects.nonNull;

public class TradeToDbEntryMapper {

    public TradeDbEntry mapToDbEntry(Trade trade) {
        var entryOrder = trade.getEntryOrder();
        var dbEntryBuilder = TradeDbEntry.builder()
            .id(trade.getId())
            .strategyExecutionId(trade.getStrategyExecutionId())
            .type(trade.getType())
            .orderFeePercent(trade.getOrderFeePercent())
            .symbol(trade.getSymbol())
            .orderExecutionType(entryOrder.getExecutionType())
            .entryOrderType(entryOrder.getType())
            .entryOrderAmount(entryOrder.getAmount())
            .entryOrderPrice(entryOrder.getPrice())
            .entryOrderTimestamp(Instant.ofEpochMilli(entryOrder.getTimestamp()));
        Optional.ofNullable(trade.getExitOrder()).ifPresent(exitOrder ->
            dbEntryBuilder
                .exitOrderType(exitOrder.getType())
                .exitOrderAmount(exitOrder.getAmount())
                .exitOrderPrice(exitOrder.getPrice())
                .exitOrderTimestamp(Instant.ofEpochMilli(exitOrder.getTimestamp())));
        return dbEntryBuilder.build();
    }

    public Trade mapToDomainObject(TradeDbEntry dbEntry) {
        var entryOrder = Order.builder()
            .type(dbEntry.getEntryOrderType())
            .executionType(dbEntry.getOrderExecutionType())
            .symbol(dbEntry.getSymbol())
            .amount(dbEntry.getEntryOrderAmount())
            .price(dbEntry.getEntryOrderPrice())
            .timestamp(dbEntry.getEntryOrderTimestamp().toEpochMilli())
            .build();
        var exitOrder = Optional.of(dbEntry)
            .filter(entry -> nonNull(dbEntry.getExitOrderType()))
            .map(entry -> Order.builder()
                .type(dbEntry.getExitOrderType())
                .executionType(dbEntry.getOrderExecutionType())
                .symbol(dbEntry.getSymbol())
                .amount(dbEntry.getExitOrderAmount())
                .price(dbEntry.getExitOrderPrice())
                .timestamp(dbEntry.getExitOrderTimestamp().toEpochMilli())
                .build())
            .orElse(null);
        return Trade.builder()
            .id(dbEntry.getId())
            .strategyExecutionId(dbEntry.getStrategyExecutionId())
            .type(dbEntry.getType())
            .orderFeePercent(dbEntry.getOrderFeePercent())
            .entryOrder(entryOrder)
            .exitOrder(exitOrder)
            .build();
    }
}
