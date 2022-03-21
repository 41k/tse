package root.tse.application.strategy_execution.rule.exit

import root.tse.application.strategy_execution.rule.RuleContext
import root.tse.application.strategy_execution.rule.RuleDescription
import root.tse.domain.ExchangeGateway
import root.tse.domain.clock.Interval
import root.tse.domain.order.Order
import spock.lang.Specification
import spock.lang.Unroll

import static root.tse.application.strategy_execution.rule.RuleParameter.COMPARISON_OPERATOR
import static root.tse.application.strategy_execution.rule.RuleParameter.TARGET_PRICE
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.*

class PriceIs_ExitRuleBuilderTest extends Specification {

    private exchangeGateway = Mock(ExchangeGateway)
    private ruleBuilder = new PriceIs_ExitRuleBuilder(exchangeGateway)

    def 'should provide rule description'() {
        expect:
        ruleBuilder.getRuleDescription() ==
            RuleDescription.builder()
                .id('6c21d09c3499')
                .name('Price is')
                .parameters([COMPARISON_OPERATOR, TARGET_PRICE])
                .build()
    }

    @Unroll
    def 'should build correct rule'() {
        given:
        def ruleContext = RuleContext.builder()
            .orderType(orderType)
            .parameters([
                (COMPARISON_OPERATOR) : comparisonOperator as String,
                (TARGET_PRICE) : targetPrice as String
            ])
            .build()

        and:
        1 * exchangeGateway.getCurrentPrices([SYMBOL_1]) >> [(SYMBOL_1) : [(BUY) : 10.5d, (SELL) : 10.21d]]
        0 * _

        and:
        def rule = ruleBuilder.build(ruleContext)

        and:
        def entryOrder = Order.builder().symbol(SYMBOL_1).timestamp(TIMESTAMP_1).build()

        expect:
        rule.getDescription() == ["Price is $comparisonOperator $targetPrice" as String]
        rule.getCheckInterval() == Interval.ONE_SECOND
        rule.isSatisfied(CLOCK_SIGNAL_2, entryOrder) == isSatisfied

        where:
        orderType | comparisonOperator | targetPrice || isSatisfied
        BUY       | '<='               | 10.55d      || true
        SELL      | '>'                | 9.7d        || true
        BUY       | '>='               | 18d         || false
        SELL      | '<'                | 6.23d       || false
    }
}
