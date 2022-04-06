package root.tse.application.rule.entry

import root.tse.application.rule.RuleContext
import root.tse.application.rule.RuleDescription
import root.tse.domain.ExchangeGateway
import root.tse.domain.clock.Interval
import spock.lang.Specification
import spock.lang.Unroll

import static root.tse.application.rule.RuleParameter.COMPARISON_OPERATOR
import static root.tse.application.rule.RuleParameter.TARGET_PRICE
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.CLOCK_SIGNAL_1
import static root.tse.util.TestUtils.SYMBOL_1

class PriceIs_EntryRuleBuilderTest extends Specification {

    private exchangeGateway = Mock(ExchangeGateway)
    private ruleBuilder = new PriceIs_EntryRuleBuilder(exchangeGateway)

    def 'should provide rule description'() {
        expect:
        ruleBuilder.getRuleDescription() ==
            RuleDescription.builder()
                .id('25e00ce8cac9')
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

        expect:
        rule.getDescription() == ["Price is $comparisonOperator $targetPrice" as String]
        rule.getCheckInterval() == Interval.ONE_SECOND
        rule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1) == isSatisfied

        where:
        orderType | comparisonOperator | targetPrice || isSatisfied
        BUY       | '<='               | 10.55d      || true
        SELL      | '>'                | 9.7d        || true
        BUY       | '>='               | 18d         || false
        SELL      | '<'                | 6.23d       || false
    }
}
