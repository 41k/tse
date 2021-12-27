package root.tse.domain.clock;

import java.util.Optional;

public class SequentialClockSignalDispatcher extends ClockSignalDispatcher {
    @Override
    public void dispatch(ClockSignal clockSignal) {
        Optional.ofNullable(intervalToConsumersMap.get(clockSignal.getInterval())).ifPresent(consumers ->
            consumers.values().forEach(consumer -> consumer.accept(clockSignal)));
    }
}
