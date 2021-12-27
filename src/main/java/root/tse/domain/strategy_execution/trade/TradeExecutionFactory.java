package root.tse.domain.strategy_execution.trade;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.MarketScanningStrategyExecution;
import root.tse.domain.clock.ClockSignalDispatcher;

@RequiredArgsConstructor
public class TradeExecutionFactory {

    private final ClockSignalDispatcher clockSignalDispatcher;

    public TradeExecution create(Trade openedTrade, MarketScanningStrategyExecution strategyExecution) {
        return TradeExecution.builder()
            .openedTrade(openedTrade)
            .exitRule(strategyExecution.getExitRule())
            .clockSignalDispatcher(clockSignalDispatcher)
            .strategyExecution(strategyExecution)
            .build();
    }
}
