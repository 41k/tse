package root.tse.domain.strategy_execution.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.strategy_execution.trade.Trade;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class StrategyExecutionEventBus {

    private static final String LOG_MSG_PREFIX = ">>> ";
    private static final String TRADE_DESCRIPTION = "trade for strategy execution [{}] and symbol [{}]";
    private static final String TRADE_WAS_OPENED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " has been opened successfully";
    private static final String TRADE_WAS_NOT_OPENED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " was not opened ";
    private static final String TRADE_WAS_CLOSED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " has been closed successfully";
    private static final String TRADE_WAS_NOT_CLOSED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " was not closed ";

    private final List<StrategyExecutionEventSubscriber> subscribers;

    public void publishTradeWasOpenedEvent(Trade openedTrade) {
        log.info(TRADE_WAS_OPENED, openedTrade.getStrategyExecutionId(), openedTrade.getSymbol());
        subscribers.forEach(subscriber -> subscriber.acceptTradeWasOpenedEvent(openedTrade));
    }

    public void publishTradeWasNotOpenedEvent(String strategyExecutionId, String symbol, String reason) {
        log.warn(TRADE_WAS_NOT_OPENED + reason, strategyExecutionId, symbol);
        subscribers.forEach(subscriber -> subscriber.acceptTradeWasNotOpenedEvent(strategyExecutionId, symbol, reason));
    }

    public void publishTradeWasClosedEvent(Trade closedTrade) {
        log.info(TRADE_WAS_CLOSED, closedTrade.getStrategyExecutionId(), closedTrade.getSymbol());
        subscribers.forEach(subscriber -> subscriber.acceptTradeWasClosedEvent(closedTrade));
    }

    public void publishTradeWasNotClosedEvent(Trade openedTrade, String reason) {
        log.error(TRADE_WAS_NOT_CLOSED + reason, openedTrade.getStrategyExecutionId(), openedTrade.getSymbol());
        subscribers.forEach(subscriber -> subscriber.acceptTradeWasNotClosedEvent(openedTrade, reason));
    }
}
