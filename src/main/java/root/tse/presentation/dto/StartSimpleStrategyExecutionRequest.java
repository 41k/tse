package root.tse.presentation.dto;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import root.tse.application.strategy_execution.rule.RuleParameter;
import root.tse.domain.order.OrderExecutionType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Validated
public class StartSimpleStrategyExecutionRequest {
    @NotNull
    private OrderExecutionType orderExecutionType;
    @NotNull
    private String symbol;
    @NotNull
    private Double fundsPerTrade;
    @NotNull
    private String entryRuleId;
    @NotNull
    private String exitRuleId;
    @NotEmpty
    private Map<RuleParameter, String> entryRuleParameters;
    @NotEmpty
    private Map<RuleParameter, String> exitRuleParameters;
}
