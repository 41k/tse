package root.tse.domain.strategy_execution.report;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.StrategyExecution;
import root.tse.domain.strategy_execution.trade.Trade;
import root.tse.domain.strategy_execution.trade.TradeRepository;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import static java.util.stream.Collectors.toList;

@RequiredArgsConstructor
public class ReportBuilder {

    private final TradeRepository tradeRepository;

    public Report build(StrategyExecution strategyExecution) {
        var strategyExecutionId = strategyExecution.getId();
        var strategyExecutionContext = strategyExecution.getContext();
        var trades = tradeRepository.getAllTradesByStrategyExecutionId(strategyExecutionId);
        return Report.builder()
            // General info
            .strategyExecutionId(strategyExecutionId)
            .symbols(strategyExecutionContext.getSymbols())
            .fundsPerTrade(strategyExecutionContext.getFundsPerTrade())
            // Performance metrics
            .equityCurve(buildEquityCurve(trades))
            .nTrades(trades.size())
            .nClosedTrades(countClosedTrades(trades))
            .nProfitableTrades(countProfitableTrades(trades))
            .totalProfit(calculateTotalProfit(trades))
            .build();
    }

    private List<EquityCurvePoint> buildEquityCurve(Collection<Trade> trades) {
        var equityCurve = new ArrayList<EquityCurvePoint>();
        var amount = 0d;
        var closedTrades = getClosedTrades(trades);
        for (var trade : closedTrades) {
            amount = amount + trade.getProfit();
            var timestamp = trade.getExitOrder().getTimestamp();
            var point = new EquityCurvePoint(timestamp, amount);
            equityCurve.add(point);
        }
        return equityCurve;
    }

    private Long countProfitableTrades(Collection<Trade> trades) {
        return trades.stream()
            .filter(Trade::isClosed)
            .mapToDouble(Trade::getProfit)
            .filter(profit -> profit > 0)
            .count();
    }

    private Double calculateTotalProfit(Collection<Trade> trades) {
        return trades.stream()
            .filter(Trade::isClosed)
            .mapToDouble(Trade::getProfit)
            .sum();
    }

    private Integer countClosedTrades(Collection<Trade> trades) {
        return getClosedTrades(trades).size();
    }

    private Collection<Trade> getClosedTrades(Collection<Trade> trades) {
        return trades.stream()
            .filter(Trade::isClosed)
            .sorted(Comparator.comparing(trade -> trade.getExitOrder().getTimestamp()))
            .collect(toList());
    }
}
