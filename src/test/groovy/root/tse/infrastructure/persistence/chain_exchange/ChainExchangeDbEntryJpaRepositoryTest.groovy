package root.tse.infrastructure.persistence.chain_exchange

import root.tse.BaseFunctionalTest

import static root.tse.util.TestUtils.CHAIN_EXCHANGE_DB_ENTRY
import static root.tse.util.TestUtils.CHAIN_EXCHANGE_ID

class ChainExchangeDbEntryJpaRepositoryTest extends BaseFunctionalTest {

    def 'should save, find and delete chain exchange'() {
        given:
        assert chainExchangeDbEntryJpaRepository.count() == 0

        when:
        chainExchangeDbEntryJpaRepository.save(CHAIN_EXCHANGE_DB_ENTRY)

        then:
        chainExchangeDbEntryJpaRepository.count() == 1

        and:
        chainExchangeDbEntryJpaRepository.findById(CHAIN_EXCHANGE_ID).get() == CHAIN_EXCHANGE_DB_ENTRY

        when:
        chainExchangeDbEntryJpaRepository.deleteById(CHAIN_EXCHANGE_ID)

        then:
        chainExchangeDbEntryJpaRepository.count() == 0
    }
}
