package root.tse.application.rule

import spock.lang.Specification

import static root.tse.application.rule.RuleParameter.*

class RuleContextTest extends Specification {

    private static final OPERATOR = '>='
    private static final PRICE = 1024.83d
    private static final INVALID_VALUE = 'invalid-value'
    private static final RULE_PARAMETERS = [
        (COMPARISON_OPERATOR.name) : OPERATOR,
        (TARGET_PRICE.name) : PRICE as String,
        (LOSS_VALUE.name) : INVALID_VALUE
    ]

    private context = RuleContext.builder().parameters(RULE_PARAMETERS).build()

    def 'should provide rule parameter successfully'() {
        expect:
        context.getParameterValue(COMPARISON_OPERATOR) == OPERATOR
        context.getParameterValue(TARGET_PRICE) == PRICE
    }

    def 'should throw exception if parameter is not found'() {
        when:
        context.getParameterValue(PROFIT_VALUE)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Rule parameter [$PROFIT_VALUE.name] is not provided" as String
    }

    def 'should throw exception if parameter has invalid value'() {
        when:
        context.getParameterValue(LOSS_VALUE)

        then:
        def exception = thrown(NumberFormatException)
        exception.message == "For input string: \"$INVALID_VALUE\"" as String
    }
}
