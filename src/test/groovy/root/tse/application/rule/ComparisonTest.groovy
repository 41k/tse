package root.tse.application.rule

import spock.lang.Specification
import spock.lang.Unroll

class ComparisonTest extends Specification {

    @Unroll
    def 'should provide correct description'() {
        expect:
        new Comparison(operator, operand2).getDescription() == description

        where:
        operator | operand2 || description
        '>'      | 45.3d    || '> 45.3'
        '>='     | 10.06d   || '>= 10.06'
        '<'      | 4d       || '< 4.0'
        '<='     | 100.7d   || '<= 100.7'
        '='      | 0d       || '= 0.0'
    }

    @Unroll
    def 'should provide correct result'() {
        expect:
        new Comparison(operator, operand2).getResult(operand1) == result

        where:
        operand1 | operator | operand2 || result
        12.03d   | '>'      | 12.02d   || true
        7d       | '>'      | 7d       || false
        8.46d    | '>='     | 8.45d    || true
        8.45d    | '>='     | 8.45d    || true
        8.44d    | '>='     | 8.45d    || false
        1.2d     | '<'      | 2d       || true
        2.2d     | '<'      | 2d       || false
        98.9d    | '<='     | 99d      || true
        99d      | '<='     | 99d      || true
        99.1d    | '<='     | 99d      || false
        15.055d  | '='      | 15.055d  || true
        15.054d  | '='      | 15.055d  || false
    }
}
