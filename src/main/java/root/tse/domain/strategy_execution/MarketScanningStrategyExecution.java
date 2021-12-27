package root.tse.domain.strategy_execution;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.Bar;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.clock.Interval;
import root.tse.domain.event.EventBus;
import root.tse.domain.strategy_execution.market_scanning.MarketScanningTask;
import root.tse.domain.strategy_execution.rule.ExitRule;
import root.tse.domain.strategy_execution.trade.*;

import java.time.Clock;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.nonNull;
import static org.apache.commons.lang3.StringUtils.EMPTY;
import static root.tse.domain.clock.Interval.ONE_MINUTE;

@Slf4j
@RequiredArgsConstructor
public class MarketScanningStrategyExecution implements StrategyExecution {

    private static final String SAME_SYMBOL_EXECUTION_REASON = "(there is a trade execution for the same symbol)";
    private static final String OPENED_TRADES_NUMBER_THRESHOLD_REASON = "(allowed number of simultaneously opened trades has been reached)";

    @Getter
    private final String id;
    @Getter
    private final StrategyExecutionContext context;
    private final ExecutorService marketScanningTaskExecutor;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final TradeService tradeService;
    private final TradeExecutionFactory tradeExecutionFactory;
    private final EventBus eventBus;
    private final Clock clock;
    private final Map<String, TradeExecution> tradeExecutions = new ConcurrentHashMap<>();

    private MarketScanningTask marketScanningTask;

    @Override
    public void start() {
        var clockSignalIntervals = Set.of(clockSignalIntervalToSubscribe());
        clockSignalDispatcher.subscribe(clockSignalIntervals, this);
    }

    @Override
    public void stop() {
        var clockSignalIntervals = Set.of(clockSignalIntervalToSubscribe());
        clockSignalDispatcher.unsubscribe(clockSignalIntervals, this);
        Optional.ofNullable(marketScanningTask).ifPresent(MarketScanningTask::stop);
        tradeExecutions.values().forEach(TradeExecution::stop);
        tradeExecutions.clear();
    }

    @Override
    public void accept(ClockSignal clockSignal) {
        if (notValid(clockSignal)) {
            return;
        }
        rerunMarketScanningTask();
    }

    public void openTrade(String symbol, Bar bar) {
        var tradeExecutionForSymbolExists = nonNull(tradeExecutions.get(symbol));
        if (tradeExecutionForSymbolExists) {
            eventBus.publishTradeWasNotOpenedEvent(id, symbol, SAME_SYMBOL_EXECUTION_REASON);
            return;
        }
        if (allowedNumberOfSimultaneouslyOpenedTradesHasBeenReached()) {
            eventBus.publishTradeWasNotOpenedEvent(id, symbol, OPENED_TRADES_NUMBER_THRESHOLD_REASON);
            return;
        }
        var tradeOpeningContext = TradeOpeningContext.builder()
            .strategyExecutionId(id)
            .orderExecutionMode(context.getOrderExecutionMode())
            .tradeType(context.getTradeType())
            .entryOrderClockSignal(entryOrderClockSignal())
            .symbol(symbol)
            .bar(bar)
            .fundsPerTrade(context.getFundsPerTrade())
            .orderFeePercent(context.getOrderFeePercent())
            .build();
        tradeService.tryToOpenTrade(tradeOpeningContext)
            .ifPresentOrElse(
                openedTrade -> {
                    startTradeExecution(openedTrade);
                    eventBus.publishTradeWasOpenedEvent(openedTrade);
                },
                () -> eventBus.publishTradeWasNotOpenedEvent(id, symbol, EMPTY));
    }

    public void closeTrade(Trade openedTrade, Bar bar) {
        var tradeClosingContext = TradeClosingContext.builder()
            .openedTrade(openedTrade)
            .bar(bar)
            .orderExecutionMode(context.getOrderExecutionMode())
            .build();
        tradeService.tryToCloseTrade(tradeClosingContext)
            .ifPresentOrElse(
                closedTrade -> {
                    stopTradeExecution(closedTrade);
                    eventBus.publishTradeWasClosedEvent(closedTrade);
                },
                () -> eventBus.publishTradeWasNotClosedEvent(openedTrade, EMPTY));
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
        var numberOfOpenedTrades = tradeService.getAllTradesByStrategyExecutionId(id).stream()
            .filter(trade -> !trade.isClosed())
            .count();
        var allowedNumberOfSimultaneouslyOpenedTrades = context.getAllowedNumberOfSimultaneouslyOpenedTrades();
        return numberOfOpenedTrades >= allowedNumberOfSimultaneouslyOpenedTrades;
    }

    private Interval clockSignalIntervalToSubscribe() {
        return context.getEntryRule().getHighestInterval();
    }

    private boolean notValid(ClockSignal clockSignal) {
        return !clockSignalIntervalToSubscribe().equals(clockSignal.getInterval());
    }

    private ClockSignal entryOrderClockSignal() {
        return new ClockSignal(ONE_MINUTE, clock.millis());
    }
}
