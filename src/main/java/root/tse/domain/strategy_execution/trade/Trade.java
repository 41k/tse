package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.NonNull;
import lombok.Value;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderType;

import static java.util.Objects.nonNull;

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
    Double orderFeePercent;
    @NonNull
    Order entryOrder;
    Order exitOrder;

    public String getSymbol() {
        return entryOrder.getSymbol();
    }

    public Double getAmount() {
        return entryOrder.getAmount();
    }

    public Order formExitOrder(Long timestamp) {
        return Order.builder()
            .type(type.getExitOrderType())
            .executionType(entryOrder.getExecutionType())
            .symbol(entryOrder.getSymbol())
            .amount(entryOrder.getAmount())
            .timestamp(timestamp)
            .build();
    }

    public boolean isClosed() {
        return nonNull(entryOrder) && nonNull(exitOrder);
    }

    public Double getProfit() {
        return type.equals(TradeType.LONG) ? calculateLongTradeProfit() : calculateShortTradeProfit();
    }

    private Double calculateLongTradeProfit() {
        return isClosed() ?
            (exitOrder.getNetTotal(orderFeePercent) - entryOrder.getNetTotal(orderFeePercent)) :
            (-entryOrder.getNetTotal(orderFeePercent));
    }

    private Double calculateShortTradeProfit() {
        throw new UnsupportedOperationException("Profit calculation for SHORT trade has not been implemented yet.");
    }
}
