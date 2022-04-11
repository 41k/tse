package root.tse.presentation;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import root.tse.application.strategy_execution.StartSimpleStrategyExecutionCommand;
import root.tse.application.strategy_execution.StopStrategyExecutionCommand;
import root.tse.application.strategy_execution.StrategyExecutionService;
import root.tse.domain.strategy_execution.report.EquityCurvePoint;
import root.tse.domain.strategy_execution.report.Report;
import root.tse.presentation.dto.StartSimpleStrategyExecutionRequest;
import root.tse.presentation.dto.StrategyExecutionDto;

import javax.validation.Valid;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/strategy-executions")
@RequiredArgsConstructor
public class StrategyExecutionController {

    private final StrategyExecutionService strategyExecutionService;

    @PostMapping
    public void startStrategyExecution(@RequestBody @Valid StartSimpleStrategyExecutionRequest request) {
        var command = StartSimpleStrategyExecutionCommand.builder()
            .orderExecutionType(request.getOrderExecutionType())
            .symbol(request.getSymbol())
            .fundsPerTrade(request.getFundsPerTrade())
            .entryRuleId(request.getEntryRuleId())
            .exitRuleId(request.getExitRuleId())
            .entryRuleParameters(request.getEntryRuleParameters())
            .exitRuleParameters(request.getExitRuleParameters())
            .build();
        strategyExecutionService.handle(command);
    }

    @DeleteMapping("/{strategyExecutionId}")
    public void stopStrategyExecution(@PathVariable String strategyExecutionId) {
        var command = new StopStrategyExecutionCommand(strategyExecutionId);
        strategyExecutionService.handle(command);
    }

    @GetMapping("/{strategyExecutionId}")
    public StrategyExecutionDto getStrategyExecution(@PathVariable String strategyExecutionId) {
        var strategyExecution = strategyExecutionService.getStrategyExecution(strategyExecutionId);
        return StrategyExecutionDto.from(strategyExecution);
    }

    @GetMapping
    public Collection<StrategyExecutionDto> getStrategyExecutions() {
        return strategyExecutionService.getStrategyExecutions().stream()
            .map(StrategyExecutionDto::from)
            .collect(Collectors.toList());
    }

    @GetMapping("/{strategyExecutionId}/report")
    public Report getStrategyExecutionReport(@PathVariable String strategyExecutionId) {
        return strategyExecutionService.getStrategyExecutionReport(strategyExecutionId);
    }
}
