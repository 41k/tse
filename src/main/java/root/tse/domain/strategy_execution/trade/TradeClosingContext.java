package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.Value;
import org.ta4j.core.Bar;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderExecutionMode;

@Value
@Builder
public class TradeClosingContext {

    OrderExecutionMode orderExecutionMode;
    Trade openedTrade;
    Bar bar;

    public Order getExitOrder() {
        return Order.builder()
            .type(openedTrade.getExitOrderType())
            .symbol(openedTrade.getSymbol())
            .amount(openedTrade.getAmount())
            .price(bar.getClosePrice().doubleValue())
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
