package root.tse.domain.backtest;

import lombok.Builder;
import lombok.Getter;
import root.tse.domain.strategy_execution.Strategy;
import root.tse.domain.strategy_execution.StrategyExecutionContext;

import java.util.List;

import static root.tse.domain.strategy_execution.StrategyExecutionMode.INCUBATION;

@Builder
@Getter
public class BacktestContext {

    private final BacktestExchangeGateway backtestExchangeGateway;
    private final Strategy strategy;
    private final String dataSetName;
    private final String symbol;
    private final Double fundsPerTrade;
    private final Double transactionFeePercent;

    public StrategyExecutionContext getStrategyExecutionContext() {
        return StrategyExecutionContext.builder()
            .strategy(strategy)
            .strategyExecutionMode(INCUBATION)
            .symbols(List.of(symbol))
            .fundsPerTrade(fundsPerTrade)
            .transactionFeePercent(transactionFeePercent)
            .build();
    }

    public Long getStartTimestamp() {
        return backtestExchangeGateway.getStartTimestamp(symbol);
    }

    public Long getEndTimestamp() {
        return backtestExchangeGateway.getEndTimestamp(symbol);
    }
}
