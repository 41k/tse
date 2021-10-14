package root.tse.domain.strategy_execution.event;

import root.tse.domain.strategy_execution.trade.Trade;

public interface StrategyExecutionEventSubscriber {

    void acceptTradeWasOpenedEvent(Trade openedTrade);

    void acceptTradeWasNotOpenedEvent(String strategyExecutionId, String symbol, String reason);

    void acceptTradeWasClosedEvent(Trade closedTrade);

    void acceptTradeWasNotClosedEvent(Trade openedTrade, String reason);
}
