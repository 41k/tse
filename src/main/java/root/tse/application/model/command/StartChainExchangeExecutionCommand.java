package root.tse.application.model.command;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.OrderExecutionMode;

@Value
@Builder
public class StartChainExchangeExecutionCommand {
    Integer assetChainId;
    Double amount;
    Double minProfitThreshold;
    OrderExecutionMode orderExecutionMode;
}
