package root.tse.application.rule.entry;

import root.tse.application.rule.EntryRuleBuilder;
import root.tse.application.rule.RuleContext;
import root.tse.application.rule.RuleDescription;
import root.tse.domain.clock.Interval;
import root.tse.domain.rule.EntryRule;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class ImmediatelySatisfiedOnlyOnce_EntryRuleBuilder implements EntryRuleBuilder {

    @Override
    public RuleDescription getRuleDescription() {
        return RuleDescription.builder()
            .id("63f742fea76f")
            .name("Immediately satisfied only once")
            .build();
    }

    @Override
    public EntryRule build(RuleContext context) {
        return new EntryRule() {
            private final AtomicBoolean satisfied = new AtomicBoolean(true);
            @Override
            public Collection<String> getDescription() { return List.of(getRuleDescription().getName()); }
            @Override
            public Interval getCheckInterval() { return Interval.ONE_SECOND; }
            @Override
            public boolean isSatisfied(String symbol) {
                return satisfied.getAndSet(false);
            }
        };
    }
}
