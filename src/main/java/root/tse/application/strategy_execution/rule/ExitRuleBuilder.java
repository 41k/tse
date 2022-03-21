package root.tse.application.strategy_execution.rule;

import root.tse.domain.strategy_execution.rule.ExitRule;

public interface ExitRuleBuilder extends RuleBuilder {

    ExitRule build(RuleContext context);
}
