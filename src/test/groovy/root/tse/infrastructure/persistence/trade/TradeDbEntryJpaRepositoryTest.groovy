package root.tse.infrastructure.persistence.trade

import org.springframework.beans.factory.annotation.Autowired
import root.tse.infrastructure.persistence.BaseJpaRepositoryTest

import static root.tse.TestData.TRADE_ID
import static root.tse.TestData.TRADE_DB_ENTRY

class TradeDbEntryJpaRepositoryTest extends BaseJpaRepositoryTest {

    @Autowired
    private TradeDbEntryJpaRepository repository

    def setup() {
        cleanTable('trade')
    }

    def 'should save, find and delete trade'() {
        given:
        assert repository.findAll().size() == 0

        when:
        repository.save(TRADE_DB_ENTRY)

        then:
        repository.findAll().size() == 1

        and:
        repository.findById(TRADE_ID).get() == TRADE_DB_ENTRY

        when:
        repository.deleteById(TRADE_ID)

        then:
        repository.findAll().size() == 0
    }
}
