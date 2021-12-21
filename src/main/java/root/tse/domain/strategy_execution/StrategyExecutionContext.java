package root.tse.domain.strategy_execution;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.strategy_execution.rule.EntryRule;
import root.tse.domain.strategy_execution.rule.ExitRule;
import root.tse.domain.strategy_execution.trade.TradeType;

import java.util.List;

@Builder
@Value
public class StrategyExecutionContext {

    Strategy strategy;
    StrategyExecutionMode strategyExecutionMode;
    List<String> symbols;
    Double fundsPerTrade;
    Double transactionFeePercent;
    @Builder.Default
    int allowedNumberOfSimultaneouslyOpenedTrades = 1;

    public EntryRule getEntryRule() {
        return strategy.getEntryRule();
    }

    public ExitRule getExitRule() {
        return strategy.getExitRule();
    }

    public TradeType getTradeType() {
        return strategy.getTradeType();
    }
}
