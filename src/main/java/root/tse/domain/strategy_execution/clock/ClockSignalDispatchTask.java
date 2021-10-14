package root.tse.domain.strategy_execution.clock;

import lombok.Value;

@Value
public class ClockSignalDispatchTask implements Runnable {

    ClockSignalConsumer clockSignalConsumer;
    ClockSignal clockSignal;

    @Override
    public void run() {
        clockSignalConsumer.accept(clockSignal);
    }
}
