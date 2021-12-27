package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.Value;
import org.ta4j.core.Bar;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderExecutionMode;
import root.tse.domain.clock.ClockSignal;

@Value
@Builder
public class TradeOpeningContext {

    String strategyExecutionId;
    OrderExecutionMode orderExecutionMode;
    TradeType tradeType;
    ClockSignal entryOrderClockSignal;
    String symbol;
    Bar bar;
    Double fundsPerTrade;
    Double orderFeePercent;

    public Order getEntryOrder() {
        var price = bar.getClosePrice().doubleValue();
        return Order.builder()
            .type(tradeType.getEntryOrderType())
            .symbol(symbol)
            .amount(fundsPerTrade / price)
            .price(price)
            // todo: refactor
            // It is not correct to use bar.getEndTime() as Order timestamp
            // since bar.getEndTime() holds bar open time instead of close time
            // because currency.com provides only bar open time.
            // Probably CurrencyComExchangeGateway should be refactored
            // so that bar.getEndTime() will provide close price but be careful with backtest functionality
            .timestamp(bar.getEndTime().toInstant().toEpochMilli())
            .build();
    }
}
