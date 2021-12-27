package root.tse.domain.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.chain_exchange_execution.ChainExchange;
import root.tse.domain.strategy_execution.trade.Trade;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class EventBus {

    private static final String LOG_MSG_PREFIX = ">>> ";
    private static final String TRADE_DESCRIPTION = "trade for strategy execution [{}] and symbol [{}]";
    private static final String TRADE_WAS_OPENED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " has been opened successfully";
    private static final String TRADE_WAS_NOT_OPENED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " was not opened ";
    private static final String TRADE_WAS_CLOSED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " has been closed successfully";
    private static final String TRADE_WAS_NOT_CLOSED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " was not closed ";
    private static final String CHAIN_EXCHANGE_DESCRIPTION = "Chain exchange [{}] with asset chain [{}]";
    private static final String CHAIN_EXCHANGE_WAS_EXECUTED = LOG_MSG_PREFIX + CHAIN_EXCHANGE_DESCRIPTION + " was executed with profit [{}]";
    private static final String CHAIN_EXCHANGE_EXECUTION_FAILED = LOG_MSG_PREFIX + CHAIN_EXCHANGE_DESCRIPTION + " execution failed";
    private static final String ACCEPTANCE_FAILURE = LOG_MSG_PREFIX + "subscriber [{}] failed to accept [{}] event";

    private final List<EventSubscriber> subscribers;

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

    public void publishChainExchangeWasExecutedEvent(ChainExchange chainExchange) {
        log.info(CHAIN_EXCHANGE_WAS_EXECUTED, chainExchange.getId(), chainExchange.getAssetChain(), chainExchange.getProfit());
        subscribers.forEach(subscriber -> {
            try {
                subscriber.acceptChainExchangeWasExecutedEvent(chainExchange);
            } catch (Exception e) {
                log.error(ACCEPTANCE_FAILURE, subscriber.getClass().getSimpleName(), "chain exchange was executed", e);
            }
        });
    }

    public void publishChainExchangeExecutionFailedEvent(String chainExchangeId, String assetChain) {
        log.error(CHAIN_EXCHANGE_EXECUTION_FAILED, chainExchangeId, assetChain);
        subscribers.forEach(subscriber -> {
            try {
                subscriber.acceptChainExchangeExecutionFailedEvent(chainExchangeId, assetChain);
            } catch (Exception e) {
                log.error(ACCEPTANCE_FAILURE, subscriber.getClass().getSimpleName(), "chain exchange execution failed", e);
            }
        });
    }
}
