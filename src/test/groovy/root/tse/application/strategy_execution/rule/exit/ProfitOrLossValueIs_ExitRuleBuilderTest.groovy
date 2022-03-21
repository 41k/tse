package root.tse.application.strategy_execution.rule.exit

import root.tse.application.strategy_execution.rule.RuleContext
import root.tse.application.strategy_execution.rule.RuleDescription
import root.tse.domain.ExchangeGateway
import root.tse.domain.clock.Interval
import root.tse.domain.order.Order
import spock.lang.Specification
import spock.lang.Unroll

import static root.tse.application.strategy_execution.rule.RuleParameter.LOSS_VALUE
import static root.tse.application.strategy_execution.rule.RuleParameter.PROFIT_VALUE
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.*

class ProfitOrLossValueIs_ExitRuleBuilderTest extends Specification {

    private exchangeGateway = Mock(ExchangeGateway)
    private ruleBuilder = new ProfitOrLossValueIs_ExitRuleBuilder(exchangeGateway)

    def 'should provide rule description'() {
        expect:
        ruleBuilder.getRuleDescription() ==
            RuleDescription.builder()
                .id('39d7d3b5788b')
                .name('Profit or loss value is')
                .parameters([PROFIT_VALUE, LOSS_VALUE])
                .build()
    }

    @Unroll
    def 'should build correct rule'() {
        given:
        def acceptableProfit = '60.0'
        def acceptableLoss = '30.0'
        def ruleContext = RuleContext.builder()
            .parameters([
                (PROFIT_VALUE) : acceptableProfit,
                (LOSS_VALUE) : acceptableLoss
            ])
            .build()

        and:
        1 * exchangeGateway.getOrderFeePercent() >> ORDER_FEE_PERCENT
        1 * exchangeGateway.getCurrentPrices([SYMBOL_1]) >> [(SYMBOL_1) : [(SELL) : currentPrice]]
        0 * _

        and:
        def rule = ruleBuilder.build(ruleContext)

        and:
        def entryOrder = Order.builder()
            .type(BUY)
            .symbol(SYMBOL_1)
            .amount(1d)
            .price(3000d)
            .timestamp(TIMESTAMP_1)
            .build()

        expect:
        rule.getDescription() == ['Profit=' + acceptableProfit, 'OR', 'Loss=' + acceptableLoss]
        rule.getCheckInterval() == Interval.ONE_SECOND
        rule.isSatisfied(CLOCK_SIGNAL_2, entryOrder) == isSatisfied

        where:
        currentPrice || isSatisfied
        3080d        || true
        3020d        || false
        2990d        || false
        2980d        || true
    }
}
