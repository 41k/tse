package root.tse.presentation.dto;

import lombok.Data;
import root.tse.domain.order.OrderExecutionType;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
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
    private Map<String, String> entryRuleParameters;
    private Map<String, String> exitRuleParameters;
}
