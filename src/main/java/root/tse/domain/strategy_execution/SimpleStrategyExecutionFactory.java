package root.tse.domain.strategy_execution;

import lombok.RequiredArgsConstructor;
import root.tse.domain.IdGenerator;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.event.EventBus;
import root.tse.domain.strategy_execution.trade.TradeService;

@RequiredArgsConstructor
public class SimpleStrategyExecutionFactory {

    private final IdGenerator idGenerator;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final TradeService tradeService;
    private final EventBus eventBus;

    public SimpleStrategyExecution create(StrategyExecutionContext context) {
        var id = idGenerator.generate();
        return new SimpleStrategyExecution(id, context, clockSignalDispatcher, tradeService, eventBus);
    }
}
