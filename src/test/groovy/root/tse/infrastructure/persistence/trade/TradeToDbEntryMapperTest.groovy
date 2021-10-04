package root.tse.infrastructure.persistence.trade

import spock.lang.Specification

import static root.tse.TestData.TRADE
import static root.tse.TestData.TRADE_DB_ENTRY

class TradeToDbEntryMapperTest extends Specification {

    private mapper = new TradeToDbEntryMapper()

    def 'should map correctly'() {
        expect:
        mapper.mapToDbEntry(TRADE) == TRADE_DB_ENTRY

        and:
        mapper.mapToDomainObject(TRADE_DB_ENTRY) == TRADE
    }
}
