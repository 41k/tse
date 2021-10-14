package root.tse.domain.strategy_execution.rule

import org.ta4j.core.Bar
import root.tse.domain.strategy_execution.Interval
import root.tse.domain.strategy_execution.trade.Order
import spock.lang.Specification

import static root.tse.domain.strategy_execution.Interval.*
import static root.tse.util.TestUtils.createClockSignal

class ExitRuleTest extends Specification {

    private bar = Mock(Bar)
    private entryOrder = Order.builder().build()

    def 'should be satisfied'() {
        given:
        def lowestRequiredInterval = FOUR_HOURS
        def clockSignalInterval = FOUR_HOURS
        def clockSignal = createClockSignal(clockSignalInterval)

        and:
        def exitRule = createExitRule(lowestRequiredInterval)

        when:
        def ruleCheckResult = exitRule.check(clockSignal, entryOrder)

        then:
        ruleCheckResult.ruleWasSatisfied()
        ruleCheckResult.barOnWhichRuleWasSatisfied == bar
    }

    def 'should not be satisfied if clock signal interval does not match lowest required interval'() {
        given:
        def lowestRequiredInterval = FIVE_MINUTES
        def clockSignalInterval = ONE_MINUTE
        def clockSignal = createClockSignal(clockSignalInterval)

        and:
        def exitRule = createExitRule(lowestRequiredInterval)

        when:
        def ruleCheckResult = exitRule.check(clockSignal, entryOrder)

        then:
        !ruleCheckResult.ruleWasSatisfied()
        !ruleCheckResult.barOnWhichRuleWasSatisfied
    }

    private ExitRule createExitRule(Interval lowestRequiredInterval) {
        new ExitRule() {
            @Override
            RuleCheckResult check(Order entryOrder) { RuleCheckResult.satisfied(bar) }
            @Override
            Interval getLowestInterval() { lowestRequiredInterval }
        }
    }
}
