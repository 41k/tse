package root.tse.application.rule.entry

import root.tse.application.rule.RuleContext
import root.tse.application.rule.RuleDescription
import root.tse.domain.clock.Interval
import spock.lang.Specification

import static root.tse.util.TestUtils.CLOCK_SIGNAL_1
import static root.tse.util.TestUtils.SYMBOL_1

class ImmediatelySatisfiedOnlyOnce_EntryRuleBuilderTest extends Specification {

    private ruleBuilder = new ImmediatelySatisfiedOnlyOnce_EntryRuleBuilder()

    def 'should provide rule description'() {
        expect:
        ruleBuilder.getRuleDescription() ==
            RuleDescription.builder()
                .id('63f742fea76f')
                .name('Immediately satisfied only once')
                .build()
    }

    def 'should build correct rule'() {
        given:
        def ruleContext = RuleContext.builder().build()
        def rule = ruleBuilder.build(ruleContext)

        expect:
        rule.getDescription() == ['Immediately satisfied only once']
        rule.getCheckInterval() == Interval.ONE_SECOND

        and: 'satisfied only once'
        rule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1)
        !rule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1)
        !rule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1)
        !rule.isSatisfied(CLOCK_SIGNAL_1, SYMBOL_1)
    }
}
