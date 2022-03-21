package root.tse.domain.strategy_execution.report;

import lombok.Builder;
import lombok.Value;

import java.util.List;

@Value
@Builder
public class Report {

    String strategyExecutionId;
    List<String> symbols;
    Double fundsPerTrade;

    List<EquityCurvePoint> equityCurve;
    Number nTrades;
    Number nClosedTrades;
    Number nProfitableTrades;
    Number totalProfit;
}
