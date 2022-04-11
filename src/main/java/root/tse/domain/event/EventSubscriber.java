package root.tse.domain.event;

import root.tse.domain.chain_exchange_execution.ChainExchange;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderType;
import root.tse.domain.strategy_execution.trade.Trade;

import java.util.List;

public interface EventSubscriber {

    void acceptTradeWasOpenedEvent(Trade openedTrade);

    void acceptTradeWasNotOpenedEvent(String strategyExecutionId, String symbol, String reason);

    void acceptTradeWasClosedEvent(Trade closedTrade);

    void acceptTradeWasNotClosedEvent(Trade openedTrade, String reason);

    void acceptChainExchangeWasExecutedEvent(ChainExchange chainExchange, List<String> assetChain);

    void acceptChainExchangeExecutionFailedEvent(String chainExchangeId, List<String> assetChain);

    void acceptOrderWasExecutedEvent(String orderExecutionId, Order order);

    void acceptOrderExecutionFailedEvent(String orderExecutionId, OrderType orderType, String symbol);
}
