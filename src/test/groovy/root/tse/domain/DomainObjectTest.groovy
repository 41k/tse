package root.tse.domain

import spock.lang.Specification
import spock.lang.Unroll

class DomainObjectTest extends Specification {

    @Unroll
    def 'should perform domain logic correctly for value [#value]'() {
        given:
        def domainObject = new DomainObject()

        when:
        def result = domainObject.performDomainLogic(value)

        then:
        result == expectedResult

        where:
        value || expectedResult
        true  || 'TRUE'
        false || 'FALSE'
    }
}
