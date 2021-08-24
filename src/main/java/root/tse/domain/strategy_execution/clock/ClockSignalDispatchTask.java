package root.tse.domain.strategy_execution.clock;

import lombok.Value;

@Value
public class ClockSignalDispatchTask implements Runnable {

    ClockSignalConsumer consumer;

    @Override
    public void run() {
        consumer.acceptClockSignal();
    }
}
