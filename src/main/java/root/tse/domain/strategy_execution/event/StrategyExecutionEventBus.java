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
    private static final String ACCEPTANCE_FAILURE = LOG_MSG_PREFIX + "subscriber [{}] failed to accept [{}] event";

    private final List<StrategyExecutionEventSubscriber> subscribers;

    public void publishTradeWasOpenedEvent(Trade openedTrade) {
        log.info(TRADE_WAS_OPENED, openedTrade.getStrategyExecutionId(), openedTrade.getSymbol());
        subscribers.forEach(subscriber -> {
            try {
                subscriber.acceptTradeWasOpenedEvent(openedTrade);
            } catch (Exception e) {
                log.error(ACCEPTANCE_FAILURE, subscriber.getClass().getSimpleName(), "trade was opened", e);
            }
        });
    }

    public void publishTradeWasNotOpenedEvent(String strategyExecutionId, String symbol, String reason) {
        log.warn(TRADE_WAS_NOT_OPENED + reason, strategyExecutionId, symbol);
        subscribers.forEach(subscriber -> {
            try {
                subscriber.acceptTradeWasNotOpenedEvent(strategyExecutionId, symbol, reason);
            } catch (Exception e) {
                log.error(ACCEPTANCE_FAILURE, subscriber.getClass().getSimpleName(), "trade was not opened", e);
            }
        });
    }

    public void publishTradeWasClosedEvent(Trade closedTrade) {
        log.info(TRADE_WAS_CLOSED, closedTrade.getStrategyExecutionId(), closedTrade.getSymbol());
        subscribers.forEach(subscriber -> {
            try {
                subscriber.acceptTradeWasClosedEvent(closedTrade);
            } catch (Exception e) {
                log.error(ACCEPTANCE_FAILURE, subscriber.getClass().getSimpleName(), "trade was closed", e);
            }
        });
    }

    public void publishTradeWasNotClosedEvent(Trade openedTrade, String reason) {
        log.error(TRADE_WAS_NOT_CLOSED + reason, openedTrade.getStrategyExecutionId(), openedTrade.getSymbol());
        subscribers.forEach(subscriber -> {
            try {
                subscriber.acceptTradeWasNotClosedEvent(openedTrade, reason);
            } catch (Exception e) {
                log.error(ACCEPTANCE_FAILURE, subscriber.getClass().getSimpleName(), "trade was not closed", e);
            }
        });
    }
}
