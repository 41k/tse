package root.tse.domain.strategy_execution.rule;

import root.tse.domain.strategy_execution.clock.ClockSignal;
import root.tse.domain.strategy_execution.trade.Order;

public abstract class ExitRule extends Rule {

    public RuleCheckResult check(ClockSignal clockSignal, Order entryOrder) {
        if (notValid(clockSignal)) {
            return RuleCheckResult.notSatisfied();
        }
        return check(entryOrder);
    }

    protected abstract RuleCheckResult check(Order entryOrder);
}
