package root.tse.domain.strategy;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.ta4j.core.indicators.helpers.ClosePriceIndicator;
import org.ta4j.core.indicators.helpers.HighPriceIndicator;
import org.ta4j.core.indicators.helpers.HighestValueIndicator;
import org.ta4j.core.indicators.helpers.PreviousValueIndicator;
import org.ta4j.core.trading.rules.OverIndicatorRule;
import root.tse.domain.strategy_execution.ExchangeGateway;
import root.tse.domain.strategy_execution.Interval;
import root.tse.domain.strategy_execution.Strategy;
import root.tse.domain.strategy_execution.rule.EntryRule;
import root.tse.domain.strategy_execution.rule.ExitRule;
import root.tse.domain.strategy_execution.rule.RuleCheckResult;
import root.tse.domain.strategy_execution.trade.Order;
import root.tse.domain.strategy_execution.trade.TradeType;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractSimpleBreakoutStrategy implements Strategy {

    private final int breakoutPeriod;
    private final Interval interval;
    private final double takeProfitPercentage;
    private final double stopLossPercentage;
    private final ExchangeGateway exchangeGateway;

    @Override
    public abstract String getId();

    @Override
    public String getName() {
        return String.format("%s interval last %d breakout, TP: %2f, SL: %2f",
            interval, breakoutPeriod, takeProfitPercentage, stopLossPercentage);
    }

    @Override
    public TradeType getTradeType() {
        return TradeType.LONG;
    }

    @Override
    public EntryRule getEntryRule() {
        return new EntryRule() {
            @Override
            public RuleCheckResult check(String symbol) {
                var seriesLength = breakoutPeriod + 1;
                return exchangeGateway.getSeries(symbol, interval, seriesLength)
                    .filter(series -> series.getBarCount() == seriesLength)
                    .map(series -> {
                        var highPrice = new HighPriceIndicator(series);
                        var closePrice = new ClosePriceIndicator(series);
                        var highestHigh = new PreviousValueIndicator(new HighestValueIndicator(highPrice, breakoutPeriod));
                        var rule = new OverIndicatorRule(closePrice, highestHigh);
                        var index = series.getEndIndex();
                        return rule.isSatisfied(index) ?
                            RuleCheckResult.satisfied(series.getLastBar()) :
                            RuleCheckResult.notSatisfied();
                    })
                    .orElseGet(RuleCheckResult::notSatisfied);
            }
            @Override
            public Interval getLowestInterval() { return interval; }
            @Override
            public Interval getHighestInterval() { return interval; }
        };
    }

    @Override
    public ExitRule getExitRule() {
        return new ExitRule() {
            @Override
            public RuleCheckResult check(Order entryOrder) {
                var symbol = entryOrder.getSymbol();
                var seriesLength = 1;
                return exchangeGateway.getSeries(symbol, interval, seriesLength)
                    .filter(series -> series.getBarCount() == seriesLength)
                    .map(series -> {
                        var entryOrderPrice = entryOrder.getPrice();
                        var takeProfitThreshold = entryOrderPrice * (1 + (takeProfitPercentage / 100));
                        var stopLossThreshold = entryOrderPrice * (1 - (stopLossPercentage / 100));
                        var index = series.getEndIndex();
                        var closePrice = new ClosePriceIndicator(series).getValue(index).doubleValue();
                        return (closePrice > takeProfitThreshold || closePrice < stopLossThreshold) ?
                            RuleCheckResult.satisfied(series.getLastBar()) :
                            RuleCheckResult.notSatisfied();
                    })
                    .orElseGet(RuleCheckResult::notSatisfied);
            }
            @Override
            public Interval getLowestInterval() { return interval; }
        };
    }
}
