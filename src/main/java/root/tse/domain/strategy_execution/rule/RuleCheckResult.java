package root.tse.domain.strategy_execution.rule;

import lombok.Builder;
import lombok.Value;
import org.ta4j.core.Bar;

@Value
@Builder
public class RuleCheckResult {
    RuleCheckStatus status;
    Bar barOnWhichRuleWasSatisfied;
}
