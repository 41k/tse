package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.ClockSignalConsumer;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.MarketScanningStrategyExecution;
import root.tse.domain.rule.ExitRule;

import java.util.Set;

@Builder
public class TradeExecution implements ClockSignalConsumer {

    private final Trade openedTrade;
    private final ExitRule exitRule;
    private final ClockSignalDispatcher clockSignalDispatcher;
    private final MarketScanningStrategyExecution strategyExecution;

    @Override
    public String getId() {
        return openedTrade.getId();
    }

    public void start() {
        var clockSignalIntervals = Set.of(exitRule.getCheckInterval());
        clockSignalDispatcher.subscribe(clockSignalIntervals, this);
    }

    public void stop() {
        var clockSignalIntervals = Set.of(exitRule.getCheckInterval());
        clockSignalDispatcher.unsubscribe(clockSignalIntervals, this);
    }

    @Override
    public void accept(ClockSignal clockSignal) {
        var entryOrder = openedTrade.getEntryOrder();
        if (exitRule.isSatisfied(clockSignal, entryOrder)) {
            strategyExecution.closeTrade(openedTrade, clockSignal);
        }
    }
}
