package root.tse.domain.strategy_execution.rule

import org.ta4j.core.Bar
import spock.lang.Specification

class RuleCheckResultTest extends Specification {

    def 'result should be correct'() {
        given:
        def bar = Mock(Bar)

        when:
        def result = RuleCheckResult.satisfied(bar)

        then:
        result.ruleWasSatisfied()
        result.getBarOnWhichRuleWasSatisfied() == bar

        when:
        result = RuleCheckResult.notSatisfied()

        then:
        !result.ruleWasSatisfied()
        !result.getBarOnWhichRuleWasSatisfied()
    }
}
