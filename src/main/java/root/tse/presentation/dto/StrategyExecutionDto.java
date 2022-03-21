package root.tse.presentation.dto;

import lombok.Builder;
import lombok.Value;
import root.tse.domain.order.OrderExecutionType;
import root.tse.domain.strategy_execution.StrategyExecution;

import java.util.Collection;

@Value
@Builder
public class StrategyExecutionDto {

    String id;
    OrderExecutionType orderExecutionType;
    Collection<String> symbols;
    Double fundsPerTrade;
    Collection<String> entryRuleDescription;
    Collection<String> exitRuleDescription;

    public static StrategyExecutionDto from(StrategyExecution strategyExecution) {
        return StrategyExecutionDto.builder()
            .id(strategyExecution.getId())
            .orderExecutionType(strategyExecution.getContext().getOrderExecutionType())
            .symbols(strategyExecution.getContext().getSymbols())
            .fundsPerTrade(strategyExecution.getContext().getFundsPerTrade())
            .entryRuleDescription(strategyExecution.getContext().getEntryRule().getDescription())
            .exitRuleDescription(strategyExecution.getContext().getExitRule().getDescription())
            .build();
    }
}
