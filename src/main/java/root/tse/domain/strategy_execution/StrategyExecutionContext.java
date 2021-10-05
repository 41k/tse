package root.tse.domain.strategy_execution;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import root.tse.domain.strategy_execution.rule.EntryRule;
import root.tse.domain.strategy_execution.rule.ExitRule;
import root.tse.domain.strategy_execution.trade.TradeType;

import java.util.Set;

@Builder
@Getter
public class StrategyExecutionContext {

    private final Strategy strategy;
    private final Set<String> symbols;
    private final StrategyExecutionMode executionMode;
    @Setter
    private Integer allowedNumberOfSimultaneouslyOpenedTrades;
    @Setter
    private Double fundsPerTrade;

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
