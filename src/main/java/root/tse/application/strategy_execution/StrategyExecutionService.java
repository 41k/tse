package root.tse.application.strategy_execution;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import root.tse.application.rule.RuleContext;
import root.tse.application.rule.RuleService;
import root.tse.domain.strategy_execution.SimpleStrategyExecutionFactory;
import root.tse.domain.strategy_execution.StrategyExecution;
import root.tse.domain.strategy_execution.StrategyExecutionContext;
import root.tse.domain.strategy_execution.report.Report;
import root.tse.domain.strategy_execution.report.ReportBuilder;
import root.tse.domain.strategy_execution.trade.TradeType;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class StrategyExecutionService {

    private final RuleService ruleService;
    private final SimpleStrategyExecutionFactory simpleStrategyExecutionFactory;
    private final Map<String, StrategyExecution> strategyExecutionsStore;
    private final ReportBuilder reportBuilder;

    public void handle(StartSimpleStrategyExecutionCommand command) {
        var tradeType = TradeType.LONG; // currently only LONG trades are supported
        var entryRule = ruleService.buildEntryRule(
            RuleContext.builder()
                .ruleId(command.getEntryRuleId())
                .orderType(tradeType.getEntryOrderType())
                .parameters(command.getEntryRuleParameters())
                .build());
        var exitRule = ruleService.buildExitRule(
            RuleContext.builder()
                .ruleId(command.getExitRuleId())
                .orderType(tradeType.getExitOrderType())
                .parameters(command.getExitRuleParameters())
                .build());
        var strategyExecutionContext = StrategyExecutionContext.builder()
            .entryRule(entryRule)
            .exitRule(exitRule)
            .tradeType(tradeType)
            .orderExecutionType(command.getOrderExecutionType())
            .symbols(List.of(command.getSymbol()))
            .fundsPerTrade(command.getFundsPerTrade())
            .build();
        var strategyExecution = simpleStrategyExecutionFactory.create(strategyExecutionContext);
        var strategyExecutionId = strategyExecution.getId();
        strategyExecution.start();
        strategyExecutionsStore.put(strategyExecutionId, strategyExecution);
        log.info(">>> simple strategy execution [{}] has been started", strategyExecutionId);
    }

    public void handle(StopStrategyExecutionCommand command) {
        var strategyExecutionId = command.getStrategyExecutionId();
        getStrategyExecution(strategyExecutionId).stop();
        strategyExecutionsStore.remove(strategyExecutionId);
        log.info(">>> strategy execution [{}] has been stopped", strategyExecutionId);
    }

    public StrategyExecution getStrategyExecution(String strategyExecutionId) {
        return Optional.ofNullable(strategyExecutionsStore.get(strategyExecutionId))
            .orElseThrow(() -> new IllegalArgumentException("Invalid strategy execution id"));
    }

    public Collection<StrategyExecution> getStrategyExecutions() {
        return strategyExecutionsStore.values();
    }

    public Report getStrategyExecutionReport(String strategyExecutionId) {
        return reportBuilder.build(getStrategyExecution(strategyExecutionId));
    }
}
