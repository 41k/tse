package root.tse.application.strategy_execution.rule;

import root.tse.domain.strategy_execution.rule.EntryRule;

public interface EntryRuleBuilder extends RuleBuilder {

    EntryRule build(RuleContext context);
}
