package root.tse.domain.strategy_execution.market_scanning

import org.ta4j.core.Bar
import root.tse.domain.strategy_execution.MarketScanningStrategyExecution
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.RuleCheckResult
import spock.lang.Specification

import static root.tse.util.TestUtils.*

class MarketScanningTaskTest extends Specification {

    private currentBarForSymbol1 = Mock(Bar)
    private currentBarForSymbol3 = Mock(Bar)

    private ruleCheckResultForSymbol1 = RuleCheckResult.satisfied(currentBarForSymbol1)
    private ruleCheckResultForSymbol2 = RuleCheckResult.notSatisfied()
    private ruleCheckResultForSymbol3 = RuleCheckResult.satisfied(currentBarForSymbol3)

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
        1 * entryRule.check(SYMBOL_1) >> ruleCheckResultForSymbol1
        1 * entryRule.check(SYMBOL_2) >> ruleCheckResultForSymbol2
        1 * entryRule.check(SYMBOL_3) >> ruleCheckResultForSymbol3

        and: 'trade is opened for SYMBOL_1'
        1 * strategyExecution.openTrade(SYMBOL_1, currentBarForSymbol1)

        and: 'trade is opened for SYMBOL_3'
        1 * strategyExecution.openTrade(SYMBOL_3, currentBarForSymbol3)

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
