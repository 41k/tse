package root.tse.infrastructure.persistence.trade

import root.tse.BaseFunctionalTest

import static root.tse.util.TestUtils.*

class TradeDbEntryJpaRepositoryTest extends BaseFunctionalTest {

    def 'should save, find and delete opened trade'() {
        given:
        assert tradeDbEntryJpaRepository.count() == 0

        when:
        tradeDbEntryJpaRepository.save(OPENED_TRADE_DB_ENTRY)

        then:
        tradeDbEntryJpaRepository.count() == 1

        and:
        tradeDbEntryJpaRepository.findById(TRADE_ID).get() == OPENED_TRADE_DB_ENTRY

        when:
        tradeDbEntryJpaRepository.deleteById(TRADE_ID)

        then:
        tradeDbEntryJpaRepository.count() == 0
    }

    def 'should save, find and delete closed trade'() {
        given:
        assert tradeDbEntryJpaRepository.count() == 0

        when:
        tradeDbEntryJpaRepository.save(CLOSED_TRADE_DB_ENTRY)

        then:
        tradeDbEntryJpaRepository.count() == 1

        and:
        tradeDbEntryJpaRepository.findById(TRADE_ID).get() == CLOSED_TRADE_DB_ENTRY

        when:
        tradeDbEntryJpaRepository.deleteById(TRADE_ID)

        then:
        tradeDbEntryJpaRepository.count() == 0
    }

    def 'should find all trades by strategy execution id'() {
        given:
        def strategyExecution1Id = 'se-1'
        def tradesOfStrategyExecution1 = [
            OPENED_TRADE_DB_ENTRY.toBuilder().id('se-1-t-1').strategyExecutionId(strategyExecution1Id).build(),
            CLOSED_TRADE_DB_ENTRY.toBuilder().id('se-1-t-2').strategyExecutionId(strategyExecution1Id).build(),
            CLOSED_TRADE_DB_ENTRY.toBuilder().id('se-1-t-3').strategyExecutionId(strategyExecution1Id).build(),
            OPENED_TRADE_DB_ENTRY.toBuilder().id('se-1-t-4').strategyExecutionId(strategyExecution1Id).build()
        ]
        def strategyExecution2Id = 'se-2'
        def tradesOfStrategyExecution2 = [
            CLOSED_TRADE_DB_ENTRY.toBuilder().id('se-2-t-1').strategyExecutionId(strategyExecution2Id).build(),
            OPENED_TRADE_DB_ENTRY.toBuilder().id('se-2-t-2').strategyExecutionId(strategyExecution2Id).build()
        ]

        and:
        assert tradeDbEntryJpaRepository.count() == 0

        when:
        tradeDbEntryJpaRepository.saveAll(tradesOfStrategyExecution1)
        tradeDbEntryJpaRepository.saveAll(tradesOfStrategyExecution2)

        then:
        tradeDbEntryJpaRepository.count() == 6

        expect:
        tradeDbEntryJpaRepository.findAllByStrategyExecutionId(strategyExecution1Id) == tradesOfStrategyExecution1

        and:
        tradeDbEntryJpaRepository.findAllByStrategyExecutionId(strategyExecution2Id) == tradesOfStrategyExecution2
    }
}
