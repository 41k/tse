package root.tse.domain.strategy_execution.rule;

import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.Interval;

import java.util.Collection;
import java.util.List;

public abstract class EntryRule implements Rule {

    @Override
    public Collection<String> getDescription() { return List.of("abstract-entry-rule"); }
    @Override
    public Interval getCheckInterval() { return Interval.ONE_DAY; }

    public boolean isSatisfied(ClockSignal clockSignal, String symbol) {
        return getCheckInterval().equals(clockSignal.getInterval())
            && isSatisfied(symbol);
    }

    public abstract boolean isSatisfied(String symbol);
}
