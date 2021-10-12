package root.tse.domain.strategy_execution.rule;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.ta4j.core.Bar;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public class RuleCheckResult {

    private static final RuleCheckResult RULE_WAS_NOT_SATISFIED_RESULT = new RuleCheckResult(false, null);

    private final boolean ruleWasSatisfied;
    @Getter
    private final Bar barOnWhichRuleWasSatisfied;

    public static RuleCheckResult satisfied(Bar barOnWhichRuleWasSatisfied) {
        return new RuleCheckResult(true, barOnWhichRuleWasSatisfied);
    }

    public static RuleCheckResult notSatisfied() {
        return RULE_WAS_NOT_SATISFIED_RESULT;
    }

    public boolean ruleWasSatisfied() {
        return ruleWasSatisfied;
    }
}
