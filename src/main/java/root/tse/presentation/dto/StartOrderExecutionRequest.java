package root.tse.presentation.dto;

import lombok.Data;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.order.OrderType;

import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
public class StartOrderExecutionRequest {
    @NotNull
    OrderType orderType;
    @NotNull
    OrderExecutionType orderExecutionType;
    @NotNull
    String symbol;
    @NotNull
    Double amount;
    @NotNull
    String ruleId;
    Map<String, String> ruleParameters;
}
