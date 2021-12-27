package root.tse.domain.strategy_execution;

import lombok.RequiredArgsConstructor;
import root.tse.domain.IdGenerator;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.event.EventBus;
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory;
import root.tse.domain.strategy_execution.trade.TradeService;

import java.time.Clock;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class MarketScanningStrategyExecutionFactory {

    private final IdGenerator idGenerator;
    private final ExecutorService marketScanningTaskExecutor;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final TradeService tradeService;
    private final TradeExecutionFactory tradeExecutionFactory;
    private final EventBus eventBus;
    private final Clock clock;

    public MarketScanningStrategyExecution create(StrategyExecutionContext context) {
        var id = idGenerator.generateId();
        return new MarketScanningStrategyExecution(
            id, context, marketScanningTaskExecutor,
            clockSignalDispatcher, tradeService, tradeExecutionFactory, eventBus, clock);
    }
}
