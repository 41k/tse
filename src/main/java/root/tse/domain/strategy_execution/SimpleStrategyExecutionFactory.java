package root.tse.domain.strategy_execution;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus;
import root.tse.domain.strategy_execution.trade.TradeService;

import java.util.UUID;

@RequiredArgsConstructor
public class SimpleStrategyExecutionFactory {

    private final ClockSignalDispatcher clockSignalDispatcher;
    private final TradeService tradeService;
    private final StrategyExecutionEventBus eventBus;

    public SimpleStrategyExecution create(StrategyExecutionContext context) {
        var strategyExecutionId = UUID.randomUUID().toString();
        return new SimpleStrategyExecution(strategyExecutionId, context, clockSignalDispatcher, tradeService, eventBus);
    }
}
