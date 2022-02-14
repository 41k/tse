package root.tse.domain.strategy_execution.rule;

import root.tse.domain.clock.Interval;

import java.util.Collection;

public interface Rule {

    Collection<String> getDescription();

    Interval getCheckInterval();
}
