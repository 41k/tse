package root.tse.domain.strategy_execution.rule

import org.ta4j.core.Bar
import root.tse.domain.clock.Interval
import spock.lang.Specification

import static root.tse.domain.clock.Interval.*
import static root.tse.util.TestUtils.SYMBOL_1
import static root.tse.util.TestUtils.createClockSignal

class EntryRuleTest extends Specification {

    private bar = Mock(Bar)

    def 'should be satisfied'() {
        given:
        def lowestRequiredInterval = FOUR_HOURS
        def clockSignalInterval = FOUR_HOURS
        def clockSignal = createClockSignal(clockSignalInterval)

        and:
        def entryRule = createEntryRule(lowestRequiredInterval)

        when:
        def ruleCheckResult = entryRule.check(clockSignal, SYMBOL_1)

        then:
        ruleCheckResult.ruleWasSatisfied()

        when:
        ruleCheckResult = entryRule.check(SYMBOL_1)

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
        def entryRule = createEntryRule(lowestRequiredInterval)

        when:
        def ruleCheckResult = entryRule.check(clockSignal, SYMBOL_1)

        then:
        !ruleCheckResult.ruleWasSatisfied()
        !ruleCheckResult.barOnWhichRuleWasSatisfied
    }

    private EntryRule createEntryRule(Interval lowestRequiredInterval) {
        new EntryRule() {
            @Override
            RuleCheckResult check(String symbol) { RuleCheckResult.satisfied(bar) }
            @Override
            Interval getLowestInterval() { lowestRequiredInterval }
            @Override
            Interval getHighestInterval() { ONE_DAY }
        }
    }
}
