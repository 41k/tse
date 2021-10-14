package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.Value;
import org.ta4j.core.Bar;
import root.tse.domain.strategy_execution.StrategyExecutionMode;
import root.tse.domain.strategy_execution.clock.ClockSignal;

@Value
@Builder
public class TradeOpeningContext {

    String strategyExecutionId;
    StrategyExecutionMode strategyExecutionMode;
    TradeType tradeType;
    ClockSignal entryOrderClockSignal;
    String symbol;
    Bar bar;
    Double fundsPerTrade;

    public Order getEntryOrder() {
        var price = bar.getClosePrice().doubleValue();
        return Order.builder()
            .type(tradeType.getEntryOrderType())
            .symbol(symbol)
            .amount(fundsPerTrade / price)
            .price(price)
            .timestamp(bar.getEndTime().toInstant().toEpochMilli())
            .build();
    }
}
