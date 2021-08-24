package root.tse.domain.strategy_execution.rule;

import root.tse.domain.strategy_execution.trade.Order;

public abstract class ExitRule extends Rule {

    public abstract RuleCheckResult check(Order entryOrder);
}
