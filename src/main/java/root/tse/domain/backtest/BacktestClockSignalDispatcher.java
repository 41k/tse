package root.tse.domain.backtest;

import root.tse.domain.strategy_execution.clock.ClockSignal;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;

import java.util.Optional;

public class BacktestClockSignalDispatcher extends ClockSignalDispatcher {
    @Override
    public void dispatch(ClockSignal clockSignal) {
        Optional.ofNullable(intervalToConsumersMap.get(clockSignal.getInterval())).ifPresent(consumers ->
            consumers.values().forEach(consumer -> consumer.accept(clockSignal)));
    }
}
