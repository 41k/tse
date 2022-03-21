package root.tse.application.strategy_execution;

import lombok.Builder;
import lombok.Value;
import root.tse.application.strategy_execution.rule.RuleParameter;
import root.tse.domain.order.OrderExecutionType;

import java.util.Map;

@Value
@Builder(toBuilder = true)
public class StartSimpleStrategyExecutionCommand {
    OrderExecutionType orderExecutionType;
    String symbol;
    Double fundsPerTrade;
    String entryRuleId;
    String exitRuleId;
    Map<RuleParameter, String> entryRuleParameters;
    Map<RuleParameter, String> exitRuleParameters;
}
