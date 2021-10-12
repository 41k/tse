package root.tse.infrastructure.persistence.trade

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import root.TseApp
import spock.lang.Specification

import static root.tse.util.TestData.*

@SpringBootTest(classes = TseApp)
class TradeDbEntryJpaRepositoryTest extends Specification {

    @Autowired
    private TradeDbEntryJpaRepository repository

    def setup() {
        repository.deleteAll()
    }

    def 'should save, find and delete opened trade'() {
        given:
        assert repository.count() == 0

        when:
        repository.save(OPENED_TRADE_DB_ENTRY)

        then:
        repository.count() == 1

        and:
        repository.findById(TRADE_ID).get() == OPENED_TRADE_DB_ENTRY

        when:
        repository.deleteById(TRADE_ID)

        then:
        repository.count() == 0
    }

    def 'should save, find and delete closed trade'() {
        given:
        assert repository.count() == 0

        when:
        repository.save(CLOSED_TRADE_DB_ENTRY)

        then:
        repository.count() == 1

        and:
        repository.findById(TRADE_ID).get() == CLOSED_TRADE_DB_ENTRY

        when:
        repository.deleteById(TRADE_ID)

        then:
        repository.count() == 0
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
        assert repository.count() == 0

        when:
        repository.saveAll(tradesOfStrategyExecution1)
        repository.saveAll(tradesOfStrategyExecution2)

        then:
        repository.count() == 6

        expect:
        repository.findAllByStrategyExecutionId(strategyExecution1Id) == tradesOfStrategyExecution1

        and:
        repository.findAllByStrategyExecutionId(strategyExecution2Id) == tradesOfStrategyExecution2
    }
}
