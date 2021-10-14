package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.Value;
import org.ta4j.core.Bar;
import root.tse.domain.strategy_execution.StrategyExecutionMode;

@Value
@Builder
public class TradeClosingContext {

    StrategyExecutionMode strategyExecutionMode;
    Trade openedTrade;
    Bar bar;

    public Order getExitOrder() {
        return Order.builder()
            .type(openedTrade.getExitOrderType())
            .symbol(openedTrade.getSymbol())
            .amount(openedTrade.getAmount())
            .price(bar.getClosePrice().doubleValue())
            .timestamp(bar.getEndTime().toInstant().toEpochMilli())
            .build();
    }
}
