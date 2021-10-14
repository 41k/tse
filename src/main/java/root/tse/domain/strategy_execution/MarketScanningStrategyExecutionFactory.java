package root.tse.domain.strategy_execution;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus;
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory;
import root.tse.domain.strategy_execution.trade.TradeService;

import java.time.Clock;
import java.util.UUID;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class MarketScanningStrategyExecutionFactory {

    private final ExecutorService marketScanningTaskExecutor;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final TradeService tradeService;
    private final TradeExecutionFactory tradeExecutionFactory;
    private final StrategyExecutionEventBus eventBus;
    private final Clock clock;

    public MarketScanningStrategyExecution create(StrategyExecutionContext context) {
        var strategyExecutionId = UUID.randomUUID().toString();
        return new MarketScanningStrategyExecution(
            strategyExecutionId, context, marketScanningTaskExecutor,
            clockSignalDispatcher, tradeService, tradeExecutionFactory, eventBus, clock);
    }
}
