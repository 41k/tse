package root.tse.domain.strategy_execution;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus;
import root.tse.domain.strategy_execution.trade.OrderExecutor;
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory;
import root.tse.domain.strategy_execution.trade.TradeRepository;

import java.util.UUID;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class StrategyExecutionFactory {

    private final ExecutorService marketScanningTaskExecutor;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final OrderExecutor orderExecutor;
    private final TradeExecutionFactory tradeExecutionFactory;
    private final TradeRepository tradeRepository;
    private final StrategyExecutionEventBus eventBus;

    public StrategyExecution create(StrategyExecutionContext context) {
        var strategyExecutionId = UUID.randomUUID().toString();
        return new StrategyExecution(
            strategyExecutionId, context, marketScanningTaskExecutor, clockSignalDispatcher, orderExecutor,
            tradeExecutionFactory, tradeRepository, eventBus);
    }
}
