package root.tse.domain.strategy_execution.market_scanning;

import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import root.tse.domain.strategy_execution.StrategyExecution;
import root.tse.domain.strategy_execution.rule.EntryRule;

import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import static root.tse.domain.strategy_execution.rule.RuleCheckStatus.SATISFIED;

@Slf4j
@Builder
public class MarketScanningTask implements Runnable {

    private final EntryRule entryRule;
    private final Set<String> symbols;
    private final StrategyExecution strategyExecution;
    @Builder.Default
    private final AtomicBoolean shouldScan = new AtomicBoolean(true);

    @Override
    public void run() {
        log.info(">>> Market scanning task for strategy execution [{}] has been started.", strategyExecution.getId());
        var checkedSymbols = new ArrayList<String>();
        for (String symbol : symbols) {
            var shouldNotScan = !shouldScan.get();
            if (shouldNotScan) {
                break;
            }
            var checkedSymbol = check(symbol);
            checkedSymbols.add(checkedSymbol);
        }
        log.info(">>> Market scanning task for strategy execution [{}] has been finished/stopped. " +
                "{} of {} symbols have been checked.", strategyExecution.getId(), checkedSymbols.size(), symbols.size());
    }

    public void stop() {
        shouldScan.set(false);
    }

    private String check(String symbol) {
        var ruleCheckResult = entryRule.check(symbol);
        var entryRuleWasSatisfied = SATISFIED.equals(ruleCheckResult.getStatus());
        if (entryRuleWasSatisfied) {
            var bar = ruleCheckResult.getBarOnWhichRuleWasSatisfied();
            strategyExecution.openTrade(symbol, bar);
        }
        return symbol;
    }
}
