package root.tse.application.strategy_execution.rule

import spock.lang.Specification

import static root.tse.application.strategy_execution.rule.RuleParameter.*

class RuleContextTest extends Specification {

    private static final OPERATOR = '>='
    private static final PRICE = 1024.83d
    private static final INVALID_VALUE = 'invalid-value'
    private static final RULE_PARAMETERS = [
        (COMPARISON_OPERATOR) : OPERATOR,
        (TARGET_PRICE) : PRICE as String,
        (LOSS_VALUE) : INVALID_VALUE
    ]

    private context = RuleContext.builder().parameters(RULE_PARAMETERS).build()

    def 'should provide rule parameter successfully'() {
        expect:
        context.getParameterValue(COMPARISON_OPERATOR, {value -> String.valueOf(value) }) == OPERATOR
        context.getParameterValue(TARGET_PRICE, {value -> Double.valueOf(value) }) == PRICE
    }

    def 'should throw exception if parameter is not found'() {
        when:
        context.getParameterValue(PROFIT_VALUE, {value -> Double.valueOf(value) })

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Rule parameter $PROFIT_VALUE is not provided" as String
    }

    def 'should throw exception if parameter has invalid value'() {
        when:
        context.getParameterValue(LOSS_VALUE, {value -> Double.valueOf(value) })

        then:
        def exception = thrown(NumberFormatException)
        exception.message == "For input string: \"$INVALID_VALUE\"" as String
    }
}
