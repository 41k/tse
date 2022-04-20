package root.tse.infrastructure

import spock.lang.Specification

class IdGeneratorImplTest extends Specification {

    private idGenerator = new IdGeneratorImpl()

    def 'should generate id'() {
        expect:
        1.upto(20, {
            assert idGenerator.generate().length() == 8
        })
    }
}
