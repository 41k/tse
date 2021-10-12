package root.tse.domain.strategy_execution.rule;

import root.tse.domain.strategy_execution.Interval;
import root.tse.domain.strategy_execution.trade.Order;

public interface ExitRule {

    RuleCheckResult check(Order entryOrder);

    Interval getLowestInterval();
}
