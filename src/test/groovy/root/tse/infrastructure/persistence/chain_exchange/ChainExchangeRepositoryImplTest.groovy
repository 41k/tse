package root.tse.infrastructure.persistence.chain_exchange

import spock.lang.Specification

import static root.tse.util.TestUtils.CHAIN_EXCHANGE_DB_ENTRY
import static root.tse.util.TestUtils.EXECUTED_CHAIN_EXCHANGE

class ChainExchangeRepositoryImplTest extends Specification {

    private mapper = Mock(ChainExchangeToDbEntryMapper)
    private dbEntryJpaRepository = Mock(ChainExchangeDbEntryJpaRepository)
    private chainExchangeRepository = new ChainExchangeRepositoryImpl(mapper, dbEntryJpaRepository)

    def 'should save trade'() {
        when:
        chainExchangeRepository.save(EXECUTED_CHAIN_EXCHANGE)

        then:
        1 * mapper.mapToDbEntry(EXECUTED_CHAIN_EXCHANGE) >> CHAIN_EXCHANGE_DB_ENTRY
        1 * dbEntryJpaRepository.saveAndFlush(CHAIN_EXCHANGE_DB_ENTRY)
        0 * _
    }
}
