package root.tse.infrastructure.persistence.chain_exchange

import spock.lang.Specification

import static root.tse.util.TestUtils.CHAIN_EXCHANGE_DB_ENTRY
import static root.tse.util.TestUtils.EXECUTED_CHAIN_EXCHANGE

class ChainExchangeToDbEntryMapperTest extends Specification {

    private mapper = new ChainExchangeToDbEntryMapper()

    def 'should map correctly'() {
        expect:
        mapper.mapToDbEntry(EXECUTED_CHAIN_EXCHANGE) == CHAIN_EXCHANGE_DB_ENTRY

        and:
        mapper.mapToDomainObject(CHAIN_EXCHANGE_DB_ENTRY) == EXECUTED_CHAIN_EXCHANGE
    }
}
