package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.strategy_execution.MarketScanningStrategyExecution;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.ClockSignalConsumer;
import root.tse.domain.clock.ClockSignalDispatcher;
import root.tse.domain.strategy_execution.rule.ExitRule;

import java.util.Set;

@Slf4j
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
        var clockSignalIntervals = Set.of(exitRule.getLowestInterval());
        clockSignalDispatcher.subscribe(clockSignalIntervals, this);
    }

    public void stop() {
        var clockSignalIntervals = Set.of(exitRule.getLowestInterval());
        clockSignalDispatcher.unsubscribe(clockSignalIntervals, this);
    }

    @Override
    public void accept(ClockSignal clockSignal) {
        if (openedTrade.getEntryOrderClockSignal().isBefore(clockSignal)) {
            var entryOrder = openedTrade.getEntryOrder();
            var ruleCheckResult = exitRule.check(clockSignal, entryOrder);
            if (ruleCheckResult.ruleWasSatisfied()) {
                var bar = ruleCheckResult.getBarOnWhichRuleWasSatisfied();
                strategyExecution.closeTrade(openedTrade, bar);
            }
        }
    }
}
