package root.tse.application.rule

import root.tse.domain.rule.EntryRule
import root.tse.domain.rule.ExitRule
import spock.lang.Specification

import static root.tse.util.TestUtils.ENTRY_RULE_ID
import static root.tse.util.TestUtils.EXIT_RULE_ID

class RuleServiceTest extends Specification {

    private entryRuleContext = RuleContext.builder().ruleId(ENTRY_RULE_ID).build()
    private exitRuleContext = RuleContext.builder().ruleId(EXIT_RULE_ID).build()
    private entryRule = Mock(EntryRule)
    private exitRule = Mock(ExitRule)
    private entryRuleBuilder = Mock(EntryRuleBuilder)
    private exitRuleBuilder = Mock(ExitRuleBuilder)
    private entryRuleBuilders = [(ENTRY_RULE_ID) : entryRuleBuilder]
    private exitRuleBuilders = [(EXIT_RULE_ID) : exitRuleBuilder]

    private ruleService = new RuleService(entryRuleBuilders, exitRuleBuilders)

    def 'should build entry rule successfully'() {
        given:
        1 * entryRuleBuilder.build(entryRuleContext) >> entryRule
        0 * _

        expect:
        ruleService.buildEntryRule(entryRuleContext) == entryRule
    }

    def 'should throw exception if entry rule id is invalid'() {
        when:
        ruleService.buildEntryRule(RuleContext.builder().ruleId('invalid-rule-id').build())

        then:
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid entry rule id'
    }

    def 'should build exit rule successfully'() {
        given:
        1 * exitRuleBuilder.build(exitRuleContext) >> exitRule
        0 * _

        expect:
        ruleService.buildExitRule(exitRuleContext) == exitRule
    }

    def 'should throw exception if exit rule id is invalid'() {
        when:
        ruleService.buildExitRule(RuleContext.builder().ruleId('invalid-rule-id').build())

        then:
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid exit rule id'
    }

    def 'should provide rules descriptions'() {
        given:
        def entryRuleBuilder1 = Mock(EntryRuleBuilder)
        def entryRuleBuilder2 = Mock(EntryRuleBuilder)
        def entryRuleBuilders = ['1' : entryRuleBuilder1, '2' : entryRuleBuilder2]
        def entryRuleDescription1 = RuleDescription.builder().id('1').build()
        def entryRuleDescription2 = RuleDescription.builder().id('2').build()
        def exitRuleBuilder1 = Mock(ExitRuleBuilder)
        def exitRuleBuilder2 = Mock(ExitRuleBuilder)
        def exitRuleBuilders = ['1' : exitRuleBuilder1, '2' : exitRuleBuilder2]
        def exitRuleDescription1 = RuleDescription.builder().id('3').build()
        def exitRuleDescription2 = RuleDescription.builder().id('4').build()

        and:
        def ruleService = new RuleService(entryRuleBuilders, exitRuleBuilders)

        and:
        1 * entryRuleBuilder1.getRuleDescription() >> entryRuleDescription1
        1 * entryRuleBuilder2.getRuleDescription() >> entryRuleDescription2
        1 * exitRuleBuilder1.getRuleDescription() >> exitRuleDescription1
        1 * exitRuleBuilder2.getRuleDescription() >> exitRuleDescription2
        0 * _

        expect:
        ruleService.getEntryRulesDescriptions() == [entryRuleDescription1, entryRuleDescription2]
        ruleService.getExitRulesDescriptions() == [exitRuleDescription1, exitRuleDescription2]
    }
}
