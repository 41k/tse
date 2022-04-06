package root.tse.domain.strategy_execution.market_scanning;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.strategy_execution.MarketScanningStrategyExecution;
import root.tse.domain.rule.EntryRule;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Builder
public class MarketScanningTask implements Runnable {

    private final EntryRule entryRule;
    private final List<String> symbols;
    private final MarketScanningStrategyExecution strategyExecution;
    @Builder.Default
    private final AtomicBoolean shouldScan = new AtomicBoolean(true);

    @Override
    public void run() {
        log.info(">>> market scanning task for strategy execution [{}] has been started.", strategyExecution.getId());
        var checkedSymbols = new ArrayList<String>();
        for (String symbol : symbols) {
            var shouldNotScan = !shouldScan.get();
            if (shouldNotScan) {
                break;
            }
            var checkedSymbol = check(symbol);
            checkedSymbols.add(checkedSymbol);
        }
        log.info(">>> market scanning task for strategy execution [{}] has been finished/stopped. " +
                "{} of {} symbols have been checked.", strategyExecution.getId(), checkedSymbols.size(), symbols.size());
    }

    public void stop() {
        shouldScan.set(false);
    }

    private String check(String symbol) {
        if (entryRule.isSatisfied(symbol)) {
            strategyExecution.openTrade(symbol);
        }
        return symbol;
    }
}
