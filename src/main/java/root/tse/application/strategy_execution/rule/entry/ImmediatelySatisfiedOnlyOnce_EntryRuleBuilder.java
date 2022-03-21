package root.tse.application.strategy_execution.rule.entry;

import root.tse.application.strategy_execution.rule.EntryRuleBuilder;
import root.tse.application.strategy_execution.rule.RuleContext;
import root.tse.application.strategy_execution.rule.RuleDescription;
import root.tse.domain.clock.Interval;
import root.tse.domain.strategy_execution.rule.EntryRule;

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
