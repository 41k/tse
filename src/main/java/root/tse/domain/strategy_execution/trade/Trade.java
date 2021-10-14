package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import root.tse.domain.strategy_execution.clock.ClockSignal;

@Value
@Builder(toBuilder = true)
public class Trade {

    String id;
    String strategyExecutionId;
    TradeType type;
    ClockSignal entryOrderClockSignal;
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
