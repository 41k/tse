package root.tse.domain.strategy_execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import root.tse.domain.strategy_execution.clock.ClockSignalConsumer;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus;
import root.tse.domain.strategy_execution.funds.FundsManager;
import root.tse.domain.strategy_execution.market_scanning.MarketScanningTask;
import root.tse.domain.strategy_execution.rule.ExitRule;
import root.tse.domain.strategy_execution.trade.*;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private static final String NOT_ENOUGH_FUNDS_REASON = "not enough funds for entry order";
    private static final String ENTRY_ORDER_WAS_NOT_FILLED_REASON = "entry order was not filled";
    private static final String EXIT_ORDER_WAS_NOT_FILLED_REASON = "exit order was not filled";

    private final String id;
    private final Strategy strategy;
    private final Set<String> symbols;
    private final StrategyExecutionType executionType;
    private final ExecutorService marketScanningTaskExecutor;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final FundsManager fundsManager;
    private final OrderExecutor orderExecutor;
    private final TradeExecutionFactory tradeExecutionFactory;
    private final TradeRepository tradeRepository;
    private final StrategyExecutionEventBus eventBus;
    private final Map<String, TradeExecution> tradeExecutions = new ConcurrentHashMap<>();

    private MarketScanningTask marketScanningTask;

    public void start() {
        var interval = strategy.getEntryRule().getHighestInterval();
        clockSignalDispatcher.subscribe(interval, this);
    }

    public void stop() {
        var interval = strategy.getEntryRule().getHighestInterval();
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
        var price = bar.getClosePrice().doubleValue();
        var amount = fundsManager.acquireFundsAndProvideTradeAmount(price);
        if (isNull(amount) || amount <= 0) {
            eventBus.publishTradeWasNotOpenedEvent(id, symbol, NOT_ENOUGH_FUNDS_REASON);
            log.info(TRADE_WAS_NOT_OPENED + NOT_ENOUGH_FUNDS_REASON, id, symbol);
            return;
        }
        var tradeType = strategy.getTradeType();
        var timestamp = bar.getEndTime().toInstant().toEpochMilli();
        var entryOrder = Order.builder()
            .type(tradeType.getEntryOrderType())
            .symbol(symbol)
            .amount(amount)
            .price(price)
            .timestamp(timestamp)
            .build();
        var executedEntryOrder = orderExecutor.execute(entryOrder, executionType);
        var entryOrderWasNotFilled = !FILLED.equals(executedEntryOrder.getStatus());
        if (entryOrderWasNotFilled) {
            fundsManager.returnFunds();
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
        var executedExitOrder = orderExecutor.execute(exitOrder, executionType);
        var exitOrderWasNotFilled = !FILLED.equals(executedExitOrder.getStatus());
        if (exitOrderWasNotFilled) {
            eventBus.publishTradeWasNotClosedEvent(tradeToClose, EXIT_ORDER_WAS_NOT_FILLED_REASON);
            log.error(TRADE_WAS_NOT_CLOSED + EXIT_ORDER_WAS_NOT_FILLED_REASON, id, symbol);
            return;
        }
        var closedTrade = tradeToClose.toBuilder().exitOrder(executedExitOrder).build();
        tradeRepository.save(closedTrade);
        fundsManager.returnFunds();
        stopTradeExecution(closedTrade);
        eventBus.publishTradeWasClosedEvent(closedTrade);
        log.info(TRADE_WAS_CLOSED, id, symbol);
    }

    public ExitRule getExitRule() {
        return strategy.getExitRule();
    }

    private void rerunMarketScanningTask() {
        Optional.ofNullable(marketScanningTask).ifPresent(MarketScanningTask::stop);
        this.marketScanningTask = MarketScanningTask.builder()
            .entryRule(strategy.getEntryRule())
            .symbols(symbols)
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
}
