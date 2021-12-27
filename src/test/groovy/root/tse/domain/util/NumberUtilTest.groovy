package root.tse.domain.util

import spock.lang.Specification
import spock.lang.Unroll

class NumberUtilTest extends Specification {

    @Unroll
    def 'should trim to precision'() {
        expect:
        NumberUtil.trimToPrecision(value, precision) == result

        where:
        value             | precision || result
        1045.00456788562d | 9         || 1045.004567885
        10d               | 5         || 10
        7.0d              | 2         || 7
        0d                | 3         || 0
        1234.567d         | 0         || 1234
        657.5d            | 3         || 657.5
    }
}
