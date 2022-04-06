package root.tse.application.rule;

import root.tse.domain.rule.ExitRule;

public interface ExitRuleBuilder extends RuleBuilder {

    ExitRule build(RuleContext context);
}
