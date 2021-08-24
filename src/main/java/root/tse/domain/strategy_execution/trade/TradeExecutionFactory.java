package root.tse.domain.strategy_execution.trade;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.StrategyExecution;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;

@RequiredArgsConstructor
public class TradeExecutionFactory {

    private final ClockSignalDispatcher clockSignalDispatcher;

    public TradeExecution create(Trade openedTrade, StrategyExecution strategyExecution) {
        return TradeExecution.builder()
            .openedTrade(openedTrade)
            .exitRule(strategyExecution.getExitRule())
            .clockSignalDispatcher(clockSignalDispatcher)
            .strategyExecution(strategyExecution)
            .build();
    }
}
