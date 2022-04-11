package root.tse.application.order_execution;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.order.OrderType;

import java.util.Map;

@Value
@Builder(toBuilder = true)
public class StartOrderExecutionCommand {
    OrderExecutionType orderExecutionType;
    OrderType orderType;
    String symbol;
    Double amount;
    String ruleId;
    Map<String, String> ruleParameters;
}
