package root.tse.infrastructure.persistence.trade

import spock.lang.Specification

import static root.tse.util.TestData.*

class TradeToDbEntryMapperTest extends Specification {

    private mapper = new TradeToDbEntryMapper()

    def 'should map correctly'() {
        expect:
        mapper.mapToDbEntry(OPENED_TRADE) == OPENED_TRADE_DB_ENTRY

        and:
        mapper.mapToDomainObject(OPENED_TRADE_DB_ENTRY) == OPENED_TRADE

        and:
        mapper.mapToDbEntry(CLOSED_TRADE) == CLOSED_TRADE_DB_ENTRY

        and:
        mapper.mapToDomainObject(CLOSED_TRADE_DB_ENTRY) == CLOSED_TRADE
    }
}
