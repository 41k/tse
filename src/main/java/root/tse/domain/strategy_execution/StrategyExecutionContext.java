package root.tse.domain.strategy_execution;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import root.tse.domain.strategy_execution.rule.EntryRule;
import root.tse.domain.strategy_execution.rule.ExitRule;
import root.tse.domain.strategy_execution.trade.TradeType;

import java.util.List;

@Builder
@Getter
public class StrategyExecutionContext {
    @NonNull
    private final Strategy strategy;
    @NonNull
    private final StrategyExecutionMode strategyExecutionMode;
    @NonNull
    private final List<String> symbols;
    @NonNull
    @Setter
    private Double fundsPerTrade;
    @Setter
    private Integer allowedNumberOfSimultaneouslyOpenedTrades;

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
