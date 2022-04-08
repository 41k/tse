package root.tse.application.strategy_execution

import root.tse.application.rule.RuleContext
import root.tse.application.rule.RuleService
import root.tse.domain.order.OrderType
import root.tse.domain.rule.EntryRule
import root.tse.domain.rule.ExitRule
import root.tse.domain.strategy_execution.SimpleStrategyExecution
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory
import root.tse.domain.strategy_execution.StrategyExecution
import root.tse.domain.strategy_execution.StrategyExecutionContext
import root.tse.domain.strategy_execution.report.Report
import root.tse.domain.strategy_execution.report.ReportBuilder
import root.tse.domain.strategy_execution.trade.TradeType
import spock.lang.Specification

import static root.tse.util.TestUtils.*

class StrategyExecutionServiceTest extends Specification {

    private static final RULE_PARAMETERS = [:] as Map
    private static final START_SIMPLE_STRATEGY_EXECUTION_COMMAND = StartSimpleStrategyExecutionCommand.builder()
        .orderExecutionType(ORDER_EXECUTION_TYPE).symbol(SYMBOL_1).fundsPerTrade(FUNDS_PER_TRADE).entryRuleId(ENTRY_RULE_ID)
        .exitRuleId(EXIT_RULE_ID).entryRuleParameters(RULE_PARAMETERS).exitRuleParameters(RULE_PARAMETERS).build()
    private static final ENTRY_RULE_CONTEXT = RuleContext.builder()
        .ruleId(ENTRY_RULE_ID).orderType(OrderType.BUY).parameters(RULE_PARAMETERS).build()
    private static final EXIT_RULE_CONTEXT = RuleContext.builder()
        .ruleId(EXIT_RULE_ID).orderType(OrderType.SELL).parameters(RULE_PARAMETERS).build()

    private entryRule = Mock(EntryRule)
    private exitRule = Mock(ExitRule)
    private ruleService = Mock(RuleService)
    private simpleStrategyExecutionFactory = Mock(SimpleStrategyExecutionFactory)
    private simpleStrategyExecution = Mock(SimpleStrategyExecution)
    private reportBuilder = Mock(ReportBuilder)

    private Map<String, StrategyExecution> strategyExecutionsStore
    private StrategyExecutionService strategyExecutionService

    def setup() {
        strategyExecutionsStore = [:]
        strategyExecutionService = new StrategyExecutionService(
            ruleService, simpleStrategyExecutionFactory, strategyExecutionsStore, reportBuilder)
    }

    def 'should start simple strategy execution successfully'() {
        given: 'no active strategy executions'
        assert strategyExecutionsStore.isEmpty()

        and:
        def strategyExecutionContext = StrategyExecutionContext.builder()
            .entryRule(entryRule)
            .exitRule(exitRule)
            .tradeType(TradeType.LONG)
            .orderExecutionType(ORDER_EXECUTION_TYPE)
            .symbols([SYMBOL_1])
            .fundsPerTrade(FUNDS_PER_TRADE)
            .build()

        when:
        strategyExecutionService.handle(START_SIMPLE_STRATEGY_EXECUTION_COMMAND)

        then:
        1 * ruleService.buildEntryRule(ENTRY_RULE_CONTEXT) >> entryRule
        1 * ruleService.buildExitRule(EXIT_RULE_CONTEXT) >> exitRule
        1 * simpleStrategyExecutionFactory.create(strategyExecutionContext) >> simpleStrategyExecution
        1 * simpleStrategyExecution.getId() >> STRATEGY_EXECUTION_ID
        1 * simpleStrategyExecution.start()
        0 * _

        and:
        strategyExecutionsStore.size() == 1
        strategyExecutionsStore.get(STRATEGY_EXECUTION_ID) == simpleStrategyExecution
    }

    def 'should stop strategy execution successfully'() {
        given: 'active strategy execution'
        strategyExecutionsStore.put(STRATEGY_EXECUTION_ID, simpleStrategyExecution)

        and:
        def command = new StopStrategyExecutionCommand(STRATEGY_EXECUTION_ID)

        when:
        strategyExecutionService.handle(command)

        then: 'strategy execution was stopped'
        1 * simpleStrategyExecution.stop()
        0 * _

        and: 'strategy execution was removed from store'
        !strategyExecutionsStore.get(STRATEGY_EXECUTION_ID)
    }

    def 'should not stop strategy execution if strategy execution id is invalid'() {
        given: 'active strategy execution'
        strategyExecutionsStore.put(STRATEGY_EXECUTION_ID, simpleStrategyExecution)

        and:
        def command = new StopStrategyExecutionCommand('invalid-strategy-execution-id')

        when:
        strategyExecutionService.handle(command)

        then:
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid strategy execution id'

        and: 'strategy execution was not stopped'
        strategyExecutionsStore.get(STRATEGY_EXECUTION_ID) == simpleStrategyExecution
    }

    def 'should provide strategy execution by id'() {
        given: 'active strategy execution'
        strategyExecutionsStore.put(STRATEGY_EXECUTION_ID, simpleStrategyExecution)

        when:
        def strategyExecution = strategyExecutionService.getStrategyExecution(STRATEGY_EXECUTION_ID)

        then:
        0 * _

        and:
        strategyExecution == simpleStrategyExecution
    }

    def 'should throw exception during strategy execution retrieval if strategy execution id is invalid'() {
        when:
        strategyExecutionService.getStrategyExecution('invalid-strategy-execution-id')

        then:
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid strategy execution id'
    }

    def 'should provide strategy executions'() {
        given:
        def strategyExecution1 = Mock(StrategyExecution)
        def strategyExecution2 = Mock(StrategyExecution)
        strategyExecutionsStore.put('1', strategyExecution1)
        strategyExecutionsStore.put('2', strategyExecution2)
        assert strategyExecutionsStore.size() == 2

        expect:
        strategyExecutionService.getStrategyExecutions() as List == [strategyExecution1, strategyExecution2]
    }

    def 'should provide report successfully'() {
        given: 'active strategy execution'
        strategyExecutionsStore.put(STRATEGY_EXECUTION_ID, simpleStrategyExecution)

        and:
        def report = Report.builder().strategyExecutionId(STRATEGY_EXECUTION_ID).build()

        and:
        1 * reportBuilder.build(simpleStrategyExecution) >> report
        0 * _

        expect:
        strategyExecutionService.getStrategyExecutionReport(STRATEGY_EXECUTION_ID) == report
    }

    def 'should throw exception during report retrieval if strategy execution id is invalid'() {
        when:
        strategyExecutionService.getStrategyExecutionReport('invalid-strategy-execution-id')

        then:
        0 * _

        and:
        def exception = thrown(IllegalArgumentException)
        exception.message == 'Invalid strategy execution id'
    }
}
