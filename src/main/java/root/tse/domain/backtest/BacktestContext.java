package root.tse.domain.backtest;

import lombok.Builder;
import lombok.Getter;
import root.tse.domain.strategy_execution.Strategy;
import root.tse.domain.strategy_execution.StrategyExecutionContext;

import java.util.List;

import static root.tse.domain.order.OrderExecutionMode.STUB;

@Builder
@Getter
public class BacktestContext {

    private final BacktestExchangeGateway backtestExchangeGateway;
    private final Strategy strategy;
    private final String dataSetName;
    private final String symbol;
    private final Double fundsPerTrade;
    private final Double orderFeePercent;

    public StrategyExecutionContext getStrategyExecutionContext() {
        return StrategyExecutionContext.builder()
            .strategy(strategy)
            .orderExecutionMode(STUB)
            .symbols(List.of(symbol))
            .fundsPerTrade(fundsPerTrade)
            .orderFeePercent(orderFeePercent)
            .build();
    }

    public Long getStartTimestamp() {
        return backtestExchangeGateway.getStartTimestamp(symbol);
    }

    public Long getEndTimestamp() {
        return backtestExchangeGateway.getEndTimestamp(symbol);
    }
}
