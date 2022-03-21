package root.tse.application.strategy_execution.rule.exit;

import lombok.RequiredArgsConstructor;
import root.tse.application.strategy_execution.rule.ExitRuleBuilder;
import root.tse.application.strategy_execution.rule.RuleContext;
import root.tse.application.strategy_execution.rule.RuleDescription;
import root.tse.domain.ExchangeGateway;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.Order;
import root.tse.domain.order.OrderType;
import root.tse.domain.strategy_execution.rule.ExitRule;

import java.util.Collection;
import java.util.List;

import static root.tse.application.strategy_execution.rule.RuleParameter.LOSS_VALUE;
import static root.tse.application.strategy_execution.rule.RuleParameter.PROFIT_VALUE;

// Note: should be used for LONG trades only
@RequiredArgsConstructor
public class ProfitOrLossValueIs_ExitRuleBuilder implements ExitRuleBuilder {

    private final ExchangeGateway exchangeGateway;

    @Override
    public RuleDescription getRuleDescription() {
        return RuleDescription.builder()
            .id("39d7d3b5788b")
            .name("Profit or loss value is")
            .parameters(List.of(PROFIT_VALUE, LOSS_VALUE))
            .build();
    }

    @Override
    public ExitRule build(RuleContext context) {
        var acceptableProfit = context.getParameterValue(PROFIT_VALUE, Double::valueOf);
        var acceptableLoss = context.getParameterValue(LOSS_VALUE, Double::valueOf);
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
