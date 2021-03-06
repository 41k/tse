package root.tse.application.rule.exit;

import lombok.RequiredArgsConstructor;
import root.tse.application.rule.ExitRuleBuilder;
import root.tse.application.rule.RuleContext;
import root.tse.application.rule.RuleDescription;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderType;
import root.tse.domain.rule.ExitRule;

import java.util.Collection;
import java.util.List;

import static root.tse.application.rule.RuleParameter.LOSS_VALUE;
import static root.tse.application.rule.RuleParameter.PROFIT_VALUE;

// Note: should be used for LONG trades only
@RequiredArgsConstructor
public class ProfitOrLossValueIs_ExitRuleBuilder implements ExitRuleBuilder {

    private final ExchangeGateway exchangeGateway;

    @Override
    public RuleDescription getRuleDescription() {
        return RuleDescription.builder()
            .id("39d7d3b5788b")
            .name("Profit or loss value is")
            .parameters(List.of(
                PROFIT_VALUE.getName(),
                LOSS_VALUE.getName()
            ))
            .build();
    }

    @Override
    public ExitRule build(RuleContext context) {
        var acceptableProfit = context.getParameterValue(PROFIT_VALUE);
        var acceptableLoss = context.getParameterValue(LOSS_VALUE);
        if (acceptableProfit < 0 && acceptableLoss < 0) {
            throw new IllegalArgumentException("Profit and loss values should be positive");
        }
        return new ExitRule() {
            @Override
            public Collection<String> getDescription() {
                return List.of("Profit=" + acceptableProfit, "OR", "Loss=" + acceptableLoss);
            }
            @Override
            public Interval getCheckInterval() { return Interval.ONE_SECOND; }
            @Override
            protected boolean isSatisfied(Order entryOrder) {
                var symbol = entryOrder.getSymbol();
                var orderFeePercent = exchangeGateway.getOrderFeePercent();
                return exchangeGateway.getCurrentPrices(List.of(symbol))
                    .map(currentPrices -> currentPrices.get(symbol))
                    .map(currentPrices -> currentPrices.get(OrderType.SELL))
                    .map(currentPrice -> {
                        var expectedExitOrder = Order.builder()
                            .type(OrderType.SELL)
                            .symbol(symbol)
                            .amount(entryOrder.getAmount())
                            .price(currentPrice)
                            .build();
                        var expectedProfit = expectedExitOrder.getNetTotal(orderFeePercent) - entryOrder.getNetTotal(orderFeePercent);
                        return expectedProfit > acceptableProfit || expectedProfit < -acceptableLoss;
                    })
                    .orElse(Boolean.FALSE);
            }
        };
    }
}
