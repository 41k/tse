package root.tse.domain.rule

import root.tse.domain.clock.Interval
import root.tse.domain.order.Order
import spock.lang.Specification

import static root.tse.domain.clock.Interval.*
import static root.tse.util.TestUtils.*

class ExitRuleTest extends Specification {

    private entryOrder = Order.builder().timestamp(TIMESTAMP_1).build()

    def 'should be satisfied'() {
        given:
        def ruleCheckInterval = FOUR_HOURS
        def clockSignalInterval = FOUR_HOURS
        def clockSignal = clockSignal(clockSignalInterval, TIMESTAMP_2)

        and:
        def exitRule = createExitRule(ruleCheckInterval)

        expect:
        exitRule.isSatisfied(clockSignal, entryOrder)
    }

    def 'should not be satisfied if clock signal interval does not match lowest required interval'() {
        given:
        def ruleCheckInterval = FIVE_MINUTES
        def clockSignalInterval = ONE_MINUTE
        def clockSignal = clockSignal(clockSignalInterval, TIMESTAMP_2)

        and:
        def exitRule = createExitRule(ruleCheckInterval)

        expect:
        !exitRule.isSatisfied(clockSignal, entryOrder)
    }

    def 'should not be satisfied if clock signal has  does not match lowest required interval'() {
        given:
        def ruleCheckInterval = FIVE_MINUTES
        def clockSignalInterval = ONE_MINUTE
        def clockSignal = clockSignal(clockSignalInterval, TIMESTAMP_2)

        and:
        def exitRule = createExitRule(ruleCheckInterval)

        expect:
        !exitRule.isSatisfied(clockSignal, entryOrder)
    }

    private ExitRule createExitRule(Interval ruleCheckInterval) {
        new ExitRule() {
            @Override
            Interval getCheckInterval() { ruleCheckInterval }
            @Override
            boolean isSatisfied(Order entryOrder) { true }
        }
    }
}
