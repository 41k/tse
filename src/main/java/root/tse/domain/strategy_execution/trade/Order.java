package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.Value;

import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED;
import static root.tse.domain.strategy_execution.trade.OrderStatus.NEW;

@Value
@Builder(toBuilder = true)
public class Order {

    @Builder.Default
    OrderStatus status = NEW;
    OrderType type;
    String symbol;
    Double amount;
    Double price;
    Long timestamp;

    public boolean wasNotFilled() {
        return !status.equals(FILLED);
    }

    public Double getTotal() {
        return amount * price;
    }
}
