package root.tse.domain.strategy_execution.trade;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.order.OrderExecutionType;

@Value
@Builder
public class TradeOpeningContext {

    String strategyExecutionId;
    OrderExecutionType orderExecutionType;
    TradeType tradeType;
    ClockSignal clockSignal;
    String symbol;
    Double fundsPerTrade;
    Double orderFeePercent;
}
