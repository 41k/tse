package root.tse.presentation.dto;

import lombok.Data;
import org.springframework.validation.annotation.Validated;
import root.tse.application.rule.RuleParameter;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.order.OrderType;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.Map;

@Data
@Validated
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
    @NotEmpty
    Map<RuleParameter, String> ruleParameters;
}
