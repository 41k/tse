package root.tse.infrastructure.persistence.trade

import spock.lang.Specification

import static root.tse.TestData.TRADE
import static root.tse.TestData.TRADE_DB_ENTRY

class TradeRepositoryImplTest extends Specification {

    private mapper = Mock(TradeToDbEntryMapper)
    private dbEntryRepository = Mock(TradeDbEntryJpaRepository)
    private tradeRepository = new TradeRepositoryImpl(mapper, dbEntryRepository)

    def 'should save trade'() {
        when:
        tradeRepository.save(TRADE)

        then:
        1 * mapper.mapToDbEntry(TRADE) >> TRADE_DB_ENTRY
        1 * dbEntryRepository.save(TRADE_DB_ENTRY)
        0 * _
    }
}
