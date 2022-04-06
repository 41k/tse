package root.tse.domain.strategy_execution;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.rule.EntryRule;
import root.tse.domain.rule.ExitRule;
import root.tse.domain.strategy_execution.trade.TradeType;

import java.util.List;

@Value
@Builder
public class StrategyExecutionContext {
    EntryRule entryRule;
    ExitRule exitRule;
    TradeType tradeType;
    OrderExecutionType orderExecutionType;
    List<String> symbols;
    Double fundsPerTrade;
    @Builder.Default
    Interval marketScanningInterval = Interval.ONE_DAY;
    @Builder.Default
    int allowedNumberOfSimultaneouslyOpenedTrades = 1;
}
