package root.tse.domain.strategy_execution.event;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.trade.Trade;

import java.util.List;

@RequiredArgsConstructor
public class StrategyExecutionEventBus {

    private final List<StrategyExecutionEventSubscriber> subscribers;

    public void publishTradeWasOpenedEvent(Trade openedTrade) {
        subscribers.forEach(subscriber -> subscriber.acceptTradeWasOpenedEvent(openedTrade));
    }

    public void publishTradeWasNotOpenedEvent(String strategyExecutionId, String symbol, String reason) {
        subscribers.forEach(subscriber -> subscriber.acceptTradeWasNotOpenedEvent(strategyExecutionId, symbol, reason));
    }

    public void publishTradeWasClosedEvent(Trade closedTrade) {
        subscribers.forEach(subscriber -> subscriber.acceptTradeWasClosedEvent(closedTrade));
    }

    public void publishTradeWasNotClosedEvent(Trade tradeToClose, String reason) {
        subscribers.forEach(subscriber -> subscriber.acceptTradeWasNotClosedEvent(tradeToClose, reason));
    }
}
