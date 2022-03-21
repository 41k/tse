package root.tse.application.strategy_execution.rule.entry;

import lombok.RequiredArgsConstructor;
import root.tse.application.strategy_execution.rule.Comparison;
import root.tse.application.strategy_execution.rule.EntryRuleBuilder;
import root.tse.application.strategy_execution.rule.RuleContext;
import root.tse.application.strategy_execution.rule.RuleDescription;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.clock.Interval;
import root.tse.domain.strategy_execution.rule.EntryRule;

import java.util.Collection;
import java.util.List;

import static root.tse.application.strategy_execution.rule.RuleParameter.COMPARISON_OPERATOR;
import static root.tse.application.strategy_execution.rule.RuleParameter.TARGET_PRICE;

@RequiredArgsConstructor
public class PriceIs_EntryRuleBuilder implements EntryRuleBuilder {

    private final ExchangeGateway exchangeGateway;

    @Override
    public RuleDescription getRuleDescription() {
        return RuleDescription.builder()
            .id("25e00ce8cac9")
            .name("Price is")
            .parameters(List.of(COMPARISON_OPERATOR, TARGET_PRICE))
            .build();
    }

    @Override
    public EntryRule build(RuleContext context) {
        var comparisonOperator = context.getParameterValue(COMPARISON_OPERATOR, String::valueOf);
        var targetPrice = context.getParameterValue(TARGET_PRICE, Double::valueOf);
        var comparison = new Comparison(comparisonOperator, targetPrice);
        return new EntryRule() {
            @Override
            public Collection<String> getDescription() { return List.of("Price is " + comparison.getDescription()); }
            @Override
            public Interval getCheckInterval() { return Interval.ONE_SECOND; }
            @Override
            public boolean isSatisfied(String symbol) {
                return exchangeGateway.getCurrentPrices(List.of(symbol))
                    .map(currentPrices -> currentPrices.get(symbol))
                    .map(currentPrices -> currentPrices.get(context.getOrderType()))
                    .map(comparison::getResult)
                    .orElse(Boolean.FALSE);
            }
        };
    }
}
