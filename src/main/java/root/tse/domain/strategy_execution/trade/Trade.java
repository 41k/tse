package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;

@Value
@Builder(toBuilder = true)
public class Trade {
    @NonNull
    String id;
    @NonNull
    String strategyExecutionId;
    @NonNull
    TradeType type;
    @NonNull
    Order entryOrder;
    Order exitOrder;

    public OrderType getExitOrderType() {
        return type.getExitOrderType();
    }

    public String getSymbol() {
        return entryOrder.getSymbol();
    }

    public Double getAmount() {
        return entryOrder.getAmount();
    }
}
