package root.tse.domain.rule;

import root.tse.domain.clock.Interval;

import java.util.Collection;

public interface Rule {

    Collection<String> getDescription();

    Interval getCheckInterval();
}
