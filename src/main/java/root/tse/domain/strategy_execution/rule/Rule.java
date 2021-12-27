package root.tse.domain.strategy_execution.rule;

import root.tse.domain.clock.Interval;
import root.tse.domain.clock.ClockSignal;

public abstract class Rule {

    public abstract Interval getLowestInterval();

    protected boolean notValid(ClockSignal clockSignal) {
        return !getLowestInterval().equals(clockSignal.getInterval());
    }
}
