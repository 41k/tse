package root.tse.domain.chain_exchange_execution;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.Order;

@Value
@Builder(toBuilder = true)
public class ChainExchange {
    String id;
    Integer assetChainId;
    Double orderFeePercent;
    Order order1;
    Order order2;
    Order order3;
    Double profit;
    Long timestamp;
}
