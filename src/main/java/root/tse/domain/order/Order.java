package root.tse.domain.order;

import lombok.Builder;
import lombok.Value;

import static root.tse.domain.order.OrderType.BUY;

@Value
@Builder(toBuilder = true)
public class Order {

    OrderType type;
    OrderExecutionType executionType;
    String symbol;
    Double amount;
    Double price;
    Long timestamp;

    public Double getNetTotal(Double orderFeePercent) {
        return BUY.equals(type) ?
            getTotal() + getFee(orderFeePercent) :
            getTotal() - getFee(orderFeePercent);
    }

    private Double getTotal() {
        return amount * price;
    }

    private Double getFee(Double orderFeePercent) {
        return getTotal() * (orderFeePercent / 100);
    }
}
