package root.tse.domain.strategy_execution.rule;

import root.tse.domain.strategy_execution.Interval;

public interface EntryRule {

    RuleCheckResult check(String symbol);

    Interval getHighestInterval();
}
