package root.tse.domain.strategy_execution.clock;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.Interval;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class ClockSignalDispatcher {

    private final Map<Interval, Map<String, ClockSignalConsumer>> intervalToConsumersMap = new HashMap<>();
    private final ExecutorService taskExecutor;

    public void subscribe(Interval interval, ClockSignalConsumer consumer) {
        var consumers = intervalToConsumersMap.get(interval);
        if (isNull(consumers)) {
            consumers = new HashMap<>();
            intervalToConsumersMap.put(interval, consumers);
        }
        consumers.put(consumer.getId(), consumer);
    }

    public void unsubscribe(Interval interval, ClockSignalConsumer consumer) {
        Optional.ofNullable(intervalToConsumersMap.get(interval))
            .ifPresent(consumers -> consumers.remove(consumer.getId()));
    }

    public void propagateClockSignal(Interval interval) {
        Optional.ofNullable(intervalToConsumersMap.get(interval)).ifPresent(consumers ->
            consumers.values().forEach(consumer -> taskExecutor.submit(new ClockSignalDispatchTask(consumer))));
    }
}
