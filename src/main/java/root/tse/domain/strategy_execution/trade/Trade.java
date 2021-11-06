package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.Value;
import root.tse.domain.strategy_execution.clock.ClockSignal;

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
    Double transactionFeePercent;
    ClockSignal entryOrderClockSignal;
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
        var entryOrderTotal = entryOrder.getTotal();
        var entryOrderFee = calculateOrderFee(entryOrder);
        if (isClosed()) {
            var exitOrderTotal = exitOrder.getTotal();
            var exitOrderFee = calculateOrderFee(exitOrder);
            return exitOrderTotal - entryOrderTotal - entryOrderFee - exitOrderFee;
        }
        return -(entryOrderTotal + entryOrderFee);
    }

    private Double calculateShortTradeProfit() {
        throw new UnsupportedOperationException("Profit calculation for SHORT trade has not been implemented yet.");
    }

    private Double calculateOrderFee(Order order) {
        return order.getTotal() * transactionFeePercent / 100;
    }
}
