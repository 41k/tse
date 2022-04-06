package root.tse.domain.rule

import root.tse.domain.clock.Interval
import spock.lang.Specification

import static root.tse.domain.clock.Interval.*
import static root.tse.util.TestUtils.SYMBOL_1
import static root.tse.util.TestUtils.clockSignal

class EntryRuleTest extends Specification {

    def 'should be satisfied'() {
        given:
        def ruleCheckInterval = FOUR_HOURS
        def clockSignalInterval = FOUR_HOURS
        def clockSignal = clockSignal(clockSignalInterval)

        and:
        def entryRule = createEntryRule(ruleCheckInterval)

        expect:
        entryRule.isSatisfied(clockSignal, SYMBOL_1)

        and:
        entryRule.isSatisfied(SYMBOL_1)
    }

    def 'should not be satisfied if clock signal interval does not match lowest required interval'() {
        given:
        def ruleCheckInterval = FIVE_MINUTES
        def clockSignalInterval = ONE_MINUTE
        def clockSignal = clockSignal(clockSignalInterval)

        and:
        def entryRule = createEntryRule(ruleCheckInterval)

        expect:
        !entryRule.isSatisfied(clockSignal, SYMBOL_1)
    }

    private EntryRule createEntryRule(Interval ruleCheckInterval) {
        new EntryRule() {
            @Override
            Interval getCheckInterval() { ruleCheckInterval }
            @Override
            boolean isSatisfied(String symbol) { true }
        }
    }
}
