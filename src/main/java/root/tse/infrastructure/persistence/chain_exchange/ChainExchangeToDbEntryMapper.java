package root.tse.infrastructure.persistence.chain_exchange;

import root.tse.domain.chain_exchange_execution.ChainExchange;
import root.tse.domain.order.Order;

import java.time.Instant;

public class ChainExchangeToDbEntryMapper {

    public ChainExchangeDbEntry mapToDbEntry(ChainExchange chainExchange) {
        var order1 = chainExchange.getOrder1();
        var order2 = chainExchange.getOrder2();
        var order3 = chainExchange.getOrder3();
        return ChainExchangeDbEntry.builder()
            .id(chainExchange.getId())
            .assetChainId(chainExchange.getAssetChainId())
            .orderFeePercent(chainExchange.getOrderFeePercent())
            .executionTimestamp(Instant.ofEpochMilli(chainExchange.getTimestamp()))
            .orderExecutionType(order1.getExecutionType())
            .order1Type(order1.getType())
            .order1Symbol(order1.getSymbol())
            .order1Amount(order1.getAmount())
            .order1Price(order1.getPrice())
            .order2Type(order2.getType())
            .order2Symbol(order2.getSymbol())
            .order2Amount(order2.getAmount())
            .order2Price(order2.getPrice())
            .order3Type(order3.getType())
            .order3Symbol(order3.getSymbol())
            .order3Amount(order3.getAmount())
            .order3Price(order3.getPrice())
            .profit(chainExchange.getProfit())
            .build();
    }

    public ChainExchange mapToDomainObject(ChainExchangeDbEntry dbEntry) {
        var order1 = Order.builder()
            .type(dbEntry.getOrder1Type())
            .executionType(dbEntry.getOrderExecutionType())
            .symbol(dbEntry.getOrder1Symbol())
            .amount(dbEntry.getOrder1Amount())
            .price(dbEntry.getOrder1Price())
            .build();
        var order2 = Order.builder()
            .type(dbEntry.getOrder2Type())
            .executionType(dbEntry.getOrderExecutionType())
            .symbol(dbEntry.getOrder2Symbol())
            .amount(dbEntry.getOrder2Amount())
            .price(dbEntry.getOrder2Price())
            .build();
        var order3 = Order.builder()
            .type(dbEntry.getOrder3Type())
            .executionType(dbEntry.getOrderExecutionType())
            .symbol(dbEntry.getOrder3Symbol())
            .amount(dbEntry.getOrder3Amount())
            .price(dbEntry.getOrder3Price())
            .build();
        return ChainExchange.builder()
            .id(dbEntry.getId())
            .assetChainId(dbEntry.getAssetChainId())
            .orderFeePercent(dbEntry.getOrderFeePercent())
            .order1(order1)
            .order2(order2)
            .order3(order3)
            .profit(dbEntry.getProfit())
            .timestamp(dbEntry.getExecutionTimestamp().toEpochMilli())
            .build();
    }
}
