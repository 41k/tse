package root.tse.infrastructure.persistence.trade;

import root.tse.domain.strategy_execution.trade.Order;
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
            .symbol(trade.getSymbol())
            .entryOrderType(entryOrder.getType())
            .entryOrderAmount(entryOrder.getAmount())
            .entryOrderPrice(entryOrder.getPrice())
            .entryOrderTimestamp(Instant.ofEpochMilli(entryOrder.getTimestamp()))
            .entryOrderStatus(entryOrder.getStatus());
        Optional.ofNullable(trade.getExitOrder()).ifPresent(exitOrder ->
            dbEntryBuilder
                .exitOrderType(exitOrder.getType())
                .exitOrderAmount(exitOrder.getAmount())
                .exitOrderPrice(exitOrder.getPrice())
                .exitOrderTimestamp(Instant.ofEpochMilli(exitOrder.getTimestamp()))
                .exitOrderStatus(exitOrder.getStatus()));
        return dbEntryBuilder.build();
    }

    public Trade mapToDomainObject(TradeDbEntry dbEntry) {
        var entryOrder = Order.builder()
            .status(dbEntry.getEntryOrderStatus())
            .type(dbEntry.getEntryOrderType())
            .symbol(dbEntry.getSymbol())
            .amount(dbEntry.getEntryOrderAmount())
            .price(dbEntry.getEntryOrderPrice())
            .timestamp(dbEntry.getEntryOrderTimestamp().toEpochMilli())
            .build();
        var exitOrder = Optional.of(dbEntry)
            .filter(entry -> nonNull(dbEntry.getExitOrderStatus()))
            .map(entry -> Order.builder()
                .status(dbEntry.getExitOrderStatus())
                .type(dbEntry.getExitOrderType())
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
            .entryOrder(entryOrder)
            .exitOrder(exitOrder)
            .build();
    }
}
