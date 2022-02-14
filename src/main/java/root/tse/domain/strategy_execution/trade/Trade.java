package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderType;

import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.nonNull;

@Value
@Builder(toBuilder = true)
@EqualsAndHashCode(exclude = {"tradeTypeToProfitCalculatorMap"})
public class Trade {

    private final Map<TradeType, Supplier<Double>> tradeTypeToProfitCalculatorMap = Map.of(
        TradeType.LONG, this::calculateLongTradeProfit,
        TradeType.SHORT, this::calculateShortTradeProfit
    );

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

    public OrderType getExitOrderType() {
        return type.getExitOrderType();
    }

    public String getSymbol() {
        return entryOrder.getSymbol();
    }

    public Double getAmount() {
        return entryOrder.getAmount();
    }

    public boolean isClosed() {
        return nonNull(entryOrder) && nonNull(exitOrder);
    }

    public Double getProfit() {
        return tradeTypeToProfitCalculatorMap.get(type).get();
    }

    private Double calculateLongTradeProfit() {
        if (isClosed()) {
            return exitOrder.getNetTotal(orderFeePercent) - entryOrder.getNetTotal(orderFeePercent);
        }
        return -entryOrder.getNetTotal(orderFeePercent);
    }

    private Double calculateShortTradeProfit() {
        throw new UnsupportedOperationException("Profit calculation for SHORT trade has not been implemented yet.");
    }
}
