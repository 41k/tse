package root.tse.domain.strategy_execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import root.tse.domain.strategy_execution.clock.ClockSignalConsumer;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus;
import root.tse.domain.strategy_execution.market_scanning.MarketScanningTask;
import root.tse.domain.strategy_execution.rule.ExitRule;
import root.tse.domain.strategy_execution.trade.*;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED;

@Slf4j
@RequiredArgsConstructor
@Getter
public class StrategyExecution implements ClockSignalConsumer {

    private static final String LOG_MSG_PREFIX = ">>> ";
    private static final String TRADE_DESCRIPTION = "trade for strategy execution [{}] and symbol [{}]";
    private static final String TRADE_WAS_OPENED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " has been opened successfully";
    private static final String TRADE_WAS_NOT_OPENED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " was not opened, reason: ";
    private static final String TRADE_WAS_CLOSED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " has been closed successfully";
    private static final String TRADE_WAS_NOT_CLOSED = LOG_MSG_PREFIX + TRADE_DESCRIPTION + " was not closed, reason: ";
    private static final String SAME_SYMBOL_EXECUTION_REASON = "there is trade execution for the same symbol";
    private static final String OPENED_TRADES_NUMBER_THRESHOLD_REASON = "allowed number of simultaneously opened trades has been reached";
    private static final String ENTRY_ORDER_WAS_NOT_FILLED_REASON = "entry order was not filled";
    private static final String EXIT_ORDER_WAS_NOT_FILLED_REASON = "exit order was not filled";

    private final String id;
    private final StrategyExecutionContext context;
    private final ExecutorService marketScanningTaskExecutor;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final OrderExecutor orderExecutor;
    private final TradeExecutionFactory tradeExecutionFactory;
    private final TradeRepository tradeRepository;
    private final StrategyExecutionEventBus eventBus;
    private final Map<String, TradeExecution> tradeExecutions = new ConcurrentHashMap<>();

    private MarketScanningTask marketScanningTask;

    public void start() {
        var interval = context.getEntryRule().getHighestInterval();
        clockSignalDispatcher.subscribe(interval, this);
    }

    public void stop() {
        var interval = context.getEntryRule().getHighestInterval();
        clockSignalDispatcher.unsubscribe(interval, this);
        Optional.ofNullable(marketScanningTask).ifPresent(MarketScanningTask::stop);
        marketScanningTaskExecutor.shutdownNow();
        tradeExecutions.values().forEach(TradeExecution::stop);
        tradeExecutions.clear();
    }

    @Override
    public void acceptClockSignal() {
        rerunMarketScanningTask();
    }

    public void openTrade(String symbol, Bar bar) {
        var tradeExecutionForSymbolExists = nonNull(tradeExecutions.get(symbol));
        if (tradeExecutionForSymbolExists) {
            eventBus.publishTradeWasNotOpenedEvent(id, symbol, SAME_SYMBOL_EXECUTION_REASON);
            log.info(TRADE_WAS_NOT_OPENED + SAME_SYMBOL_EXECUTION_REASON, id, symbol);
            return;
        }
        if (allowedNumberOfSimultaneouslyOpenedTradesHasBeenReached()) {
            eventBus.publishTradeWasNotOpenedEvent(id, symbol, OPENED_TRADES_NUMBER_THRESHOLD_REASON);
            log.info(TRADE_WAS_NOT_OPENED + OPENED_TRADES_NUMBER_THRESHOLD_REASON, id, symbol);
            return;
        }
        var tradeType = context.getTradeType();
        var price = bar.getClosePrice().doubleValue();
        var fundsPerTrade = context.getFundsPerTrade();
        var amount = fundsPerTrade / price;
        var timestamp = bar.getEndTime().toInstant().toEpochMilli();
        var entryOrder = Order.builder()
            .type(tradeType.getEntryOrderType())
            .symbol(symbol)
            .amount(amount)
            .price(price)
            .timestamp(timestamp)
            .build();
        var executionMode = context.getExecutionMode();
        var executedEntryOrder = orderExecutor.execute(entryOrder, executionMode);
        var entryOrderWasNotFilled = !FILLED.equals(executedEntryOrder.getStatus());
        if (entryOrderWasNotFilled) {
            eventBus.publishTradeWasNotOpenedEvent(id, symbol, ENTRY_ORDER_WAS_NOT_FILLED_REASON);
            log.error(TRADE_WAS_NOT_OPENED + ENTRY_ORDER_WAS_NOT_FILLED_REASON, id, symbol);
            return;
        }
        var openedTrade = Trade.builder()
            .id(UUID.randomUUID().toString())
            .strategyExecutionId(id)
            .type(tradeType)
            .entryOrder(executedEntryOrder)
            .build();
        tradeRepository.save(openedTrade);
        startTradeExecution(openedTrade);
        eventBus.publishTradeWasOpenedEvent(openedTrade);
        log.info(TRADE_WAS_OPENED, id, symbol);
    }

    public void closeTrade(Trade tradeToClose) {
        var symbol = tradeToClose.getSymbol();
        var exitOrder = tradeToClose.getExitOrder();
        var executionMode = context.getExecutionMode();
        var executedExitOrder = orderExecutor.execute(exitOrder, executionMode);
        var exitOrderWasNotFilled = !FILLED.equals(executedExitOrder.getStatus());
        if (exitOrderWasNotFilled) {
            eventBus.publishTradeWasNotClosedEvent(tradeToClose, EXIT_ORDER_WAS_NOT_FILLED_REASON);
            log.error(TRADE_WAS_NOT_CLOSED + EXIT_ORDER_WAS_NOT_FILLED_REASON, id, symbol);
            return;
        }
        var closedTrade = tradeToClose.toBuilder().exitOrder(executedExitOrder).build();
        tradeRepository.save(closedTrade);
        stopTradeExecution(closedTrade);
        eventBus.publishTradeWasClosedEvent(closedTrade);
        log.info(TRADE_WAS_CLOSED, id, symbol);
    }

    public ExitRule getExitRule() {
        return context.getExitRule();
    }

    private void rerunMarketScanningTask() {
        Optional.ofNullable(marketScanningTask).ifPresent(MarketScanningTask::stop);
        this.marketScanningTask = MarketScanningTask.builder()
            .entryRule(context.getEntryRule())
            .symbols(context.getSymbols())
            .strategyExecution(this)
            .build();
        marketScanningTaskExecutor.submit(marketScanningTask);
    }

    private void startTradeExecution(Trade openedTrade) {
        var tradeExecution = tradeExecutionFactory.create(openedTrade, this);
        tradeExecution.start();
        var symbol = openedTrade.getSymbol();
        tradeExecutions.put(symbol, tradeExecution);
    }

    private void stopTradeExecution(Trade closedTrade) {
        var symbol = closedTrade.getSymbol();
        var tradeExecution = tradeExecutions.remove(symbol);
        tradeExecution.stop();
    }

    private boolean allowedNumberOfSimultaneouslyOpenedTradesHasBeenReached() {
        var numberOfOpenedTrades = tradeRepository.getAllTradesByStrategyExecutionId(id).stream()
            .filter(trade -> isNull(trade.getExitOrder()))
            .count();
        var allowedNumberOfSimultaneouslyOpenedTrades = context.getAllowedNumberOfSimultaneouslyOpenedTrades();
        return numberOfOpenedTrades >= allowedNumberOfSimultaneouslyOpenedTrades;
    }
}
