package root.tse.infrastructure.persistence.trade

import spock.lang.Specification

import static root.tse.util.TestUtils.*

class TradeToDbEntryMapperTest extends Specification {

    private mapper = new TradeToDbEntryMapper()

    def 'should map correctly'() {
        given:
        // necessary since Trade#entryOrderClockSignal field is not persisted
        def openedTrade = OPENED_TRADE.toBuilder().entryOrderClockSignal(null).build()
        def closedTrade = CLOSED_TRADE.toBuilder().entryOrderClockSignal(null).build()

        expect:
        mapper.mapToDbEntry(openedTrade) == OPENED_TRADE_DB_ENTRY

        and:
        mapper.mapToDomainObject(OPENED_TRADE_DB_ENTRY) == openedTrade

        and:
        mapper.mapToDbEntry(closedTrade) == CLOSED_TRADE_DB_ENTRY

        and:
        mapper.mapToDomainObject(CLOSED_TRADE_DB_ENTRY) == closedTrade
    }
}
