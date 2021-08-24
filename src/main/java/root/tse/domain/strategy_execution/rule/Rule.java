package root.tse.domain.strategy_execution.rule;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.Interval;

@RequiredArgsConstructor
public abstract class Rule {

    public abstract Interval getLowestInterval();

    public abstract Interval getHighestInterval();
}
