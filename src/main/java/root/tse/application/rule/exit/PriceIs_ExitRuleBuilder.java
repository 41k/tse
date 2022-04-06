package root.tse.application.rule.exit;

import lombok.RequiredArgsConstructor;
import root.tse.application.rule.Comparison;
import root.tse.application.rule.ExitRuleBuilder;
import root.tse.application.rule.RuleContext;
import root.tse.application.rule.RuleDescription;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.Order;
import root.tse.domain.rule.ExitRule;

import java.util.Collection;
import java.util.List;

import static root.tse.application.rule.RuleParameter.COMPARISON_OPERATOR;
import static root.tse.application.rule.RuleParameter.TARGET_PRICE;

@RequiredArgsConstructor
public class PriceIs_ExitRuleBuilder implements ExitRuleBuilder {

    private final ExchangeGateway exchangeGateway;

    @Override
    public RuleDescription getRuleDescription() {
        return RuleDescription.builder()
            .id("6c21d09c3499")
            .name("Price is")
            .parameters(List.of(COMPARISON_OPERATOR, TARGET_PRICE))
            .build();
    }

    @Override
    public ExitRule build(RuleContext context) {
        var comparisonOperator = context.getParameterValue(COMPARISON_OPERATOR, String::valueOf);
        var targetPrice = context.getParameterValue(TARGET_PRICE, Double::valueOf);
        var comparison = new Comparison(comparisonOperator, targetPrice);
        return new ExitRule() {
            @Override
            public Collection<String> getDescription() { return List.of("Price is " + comparison.getDescription()); }
            @Override
            public Interval getCheckInterval() { return Interval.ONE_SECOND; }
            @Override
            protected boolean isSatisfied(Order entryOrder) {
                var symbol = entryOrder.getSymbol();
                return exchangeGateway.getCurrentPrices(List.of(symbol))
                    .map(currentPrices -> currentPrices.get(symbol))
                    .map(currentPrices -> currentPrices.get(context.getOrderType()))
                    .map(comparison::getResult)
                    .orElse(Boolean.FALSE);
            }
        };
    }
}
