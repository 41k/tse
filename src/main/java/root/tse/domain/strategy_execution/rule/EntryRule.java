package root.tse.domain.strategy_execution.rule;

import root.tse.domain.strategy_execution.Interval;
import root.tse.domain.strategy_execution.clock.ClockSignal;

public abstract class EntryRule extends Rule {

    public RuleCheckResult check(ClockSignal clockSignal, String symbol) {
        if (notValid(clockSignal)) {
            return RuleCheckResult.notSatisfied();
        }
        return check(symbol);
    }

    public abstract RuleCheckResult check(String symbol);

    public abstract Interval getHighestInterval();
}
