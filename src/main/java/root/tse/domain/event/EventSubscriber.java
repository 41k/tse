package root.tse.domain.event;

import root.tse.domain.chain_exchange_execution.ChainExchange;
import root.tse.domain.strategy_execution.trade.Trade;

public interface EventSubscriber {

    void acceptTradeWasOpenedEvent(Trade openedTrade);

    void acceptTradeWasNotOpenedEvent(String strategyExecutionId, String symbol, String reason);

    void acceptTradeWasClosedEvent(Trade closedTrade);

    void acceptTradeWasNotClosedEvent(Trade openedTrade, String reason);

    void acceptChainExchangeWasExecutedEvent(ChainExchange chainExchange);

    void acceptChainExchangeExecutionFailedEvent(String chainExchangeId, String assetChain);
}