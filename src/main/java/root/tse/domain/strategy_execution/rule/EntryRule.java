package root.tse.domain.strategy_execution.rule;

public abstract class EntryRule extends Rule {

    public abstract RuleCheckResult check(String symbol);
}
