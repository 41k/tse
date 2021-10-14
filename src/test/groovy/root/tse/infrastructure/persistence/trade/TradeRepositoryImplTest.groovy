package root.tse.infrastructure.persistence.trade

import spock.lang.Specification

import static root.tse.util.TestUtils.*

class TradeRepositoryImplTest extends Specification {

    private mapper = Mock(TradeToDbEntryMapper)
    private dbEntryRepository = Mock(TradeDbEntryJpaRepository)
    private tradeRepository = new TradeRepositoryImpl(mapper, dbEntryRepository)

    def 'should save trade'() {
        when:
        tradeRepository.save(CLOSED_TRADE)

        then:
        1 * mapper.mapToDbEntry(CLOSED_TRADE) >> CLOSED_TRADE_DB_ENTRY
        1 * dbEntryRepository.save(CLOSED_TRADE_DB_ENTRY)
        0 * _
    }

    def 'should get all trades by strategy execution id'() {
        when:
        def trades = tradeRepository.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID)

        then:
        1 * dbEntryRepository.findAllByStrategyExecutionId(STRATEGY_EXECUTION_ID) >> [CLOSED_TRADE_DB_ENTRY, OPENED_TRADE_DB_ENTRY]
        1 * mapper.mapToDomainObject(CLOSED_TRADE_DB_ENTRY) >> CLOSED_TRADE
        1 * mapper.mapToDomainObject(OPENED_TRADE_DB_ENTRY) >> OPENED_TRADE
        0 * _

        and:
        trades == [CLOSED_TRADE, OPENED_TRADE]
    }
}
