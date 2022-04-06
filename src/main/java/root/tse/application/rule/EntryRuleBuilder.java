package root.tse.application.rule;

import root.tse.domain.rule.EntryRule;

public interface EntryRuleBuilder extends RuleBuilder {

    EntryRule build(RuleContext context);
}
