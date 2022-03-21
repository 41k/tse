package root.tse.application.strategy_execution;

import lombok.Value;

@Value
public class StopStrategyExecutionCommand {
    String strategyExecutionId;
}
