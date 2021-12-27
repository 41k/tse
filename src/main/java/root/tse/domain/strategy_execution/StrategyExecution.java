package root.tse.domain.strategy_execution;

import root.tse.domain.clock.ClockSignalConsumer;

public interface StrategyExecution extends ClockSignalConsumer {

    void start();

    void stop();

    StrategyExecutionContext getContext();
}
