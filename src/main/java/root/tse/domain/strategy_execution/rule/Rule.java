package root.tse.domain.strategy_execution.rule;

import root.tse.domain.strategy_execution.Interval;
import root.tse.domain.strategy_execution.clock.ClockSignal;

public abstract class Rule {

    public abstract Interval getLowestInterval();

    protected boolean notValid(ClockSignal clockSignal) {
        return !getLowestInterval().equals(clockSignal.getInterval());
    }
}
