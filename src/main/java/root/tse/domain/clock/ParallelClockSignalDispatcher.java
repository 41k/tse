package root.tse.domain.clock;

import lombok.RequiredArgsConstructor;

import java.util.Optional;
import java.util.concurrent.ExecutorService;

@RequiredArgsConstructor
public class ParallelClockSignalDispatcher extends ClockSignalDispatcher {

    private final ExecutorService taskExecutor;

    @Override
    public void dispatch(ClockSignal clockSignal) {
        Optional.ofNullable(intervalToConsumersMap.get(clockSignal.getInterval())).ifPresent(consumers ->
            consumers.values().forEach(consumer -> taskExecutor.submit(new ClockSignalDispatchTask(consumer, clockSignal))));
    }
}
