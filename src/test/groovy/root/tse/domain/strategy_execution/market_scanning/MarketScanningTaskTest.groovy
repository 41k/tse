package root.tse.domain.strategy_execution.market_scanning

import root.tse.domain.strategy_execution.MarketScanningStrategyExecution
import root.tse.domain.strategy_execution.rule.EntryRule
import spock.lang.Specification

import static root.tse.util.TestUtils.*

class MarketScanningTaskTest extends Specification {

    private entryRule = Mock(EntryRule)
    private strategyExecution = Mock(MarketScanningStrategyExecution)

    private MarketScanningTask marketScanningTask

    def setup() {
        marketScanningTask = MarketScanningTask.builder()
            .entryRule(entryRule)
            .symbols(SYMBOLS)
            .strategyExecution(strategyExecution)
            .build()
    }

    def 'should check symbols and open trade for each symbol for which entry rule is satisfied'() {
        when:
        marketScanningTask.run()

        then:
        2 * strategyExecution.getId() >> STRATEGY_EXECUTION_ID

        and: 'entry rule is satisfied for SYMBOL_1 and SYMBOL_3'
        1 * entryRule.isSatisfied(SYMBOL_1) >> true
        1 * entryRule.isSatisfied(SYMBOL_2) >> false
        1 * entryRule.isSatisfied(SYMBOL_3) >> true

        and: 'trade is opened for SYMBOL_1'
        1 * strategyExecution.openTrade(SYMBOL_1)

        and: 'trade is opened for SYMBOL_3'
        1 * strategyExecution.openTrade(SYMBOL_3)

        and: 'no other actions'
        0 * _
    }

    def 'should not check symbols if task is stopped'() {
        given: 'stopped task'
        marketScanningTask.stop()

        when:
        marketScanningTask.run()

        then:
        2 * strategyExecution.getId() >> STRATEGY_EXECUTION_ID
        0 * _
    }
}
