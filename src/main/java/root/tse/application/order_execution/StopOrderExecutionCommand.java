package root.tse.application.order_execution;

import lombok.Value;

@Value
public class StopOrderExecutionCommand {
    String orderExecutionId;
}
