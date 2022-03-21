package root.tse.application.chain_exchange_execution;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.OrderExecutionType;

@Value
@Builder
public class StartChainExchangeExecutionCommand {
    Integer assetChainId;
    Double amount;
    Double minProfitThreshold;
    OrderExecutionType orderExecutionType;
}
