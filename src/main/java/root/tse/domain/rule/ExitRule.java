package root.tse.domain.rule;

import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.Order;

import java.util.Collection;
import java.util.List;

public abstract class ExitRule implements Rule {

    @Override
    public Collection<String> getDescription() { return List.of("abstract-exit-rule"); }
    @Override
    public Interval getCheckInterval() { return Interval.ONE_DAY; }

    public boolean isSatisfied(ClockSignal clockSignal, Order entryOrder) {
        return getCheckInterval().equals(clockSignal.getInterval())
            && clockSignal.getTimestamp() > entryOrder.getTimestamp()
            && isSatisfied(entryOrder);
    }

    protected abstract boolean isSatisfied(Order entryOrder);
}
