package root.tse.domain.strategy_execution.clock;

import lombok.RequiredArgsConstructor;
import root.tse.domain.strategy_execution.Interval;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

import static java.util.Objects.isNull;

@RequiredArgsConstructor
public class ClockSignalDispatcher {

    private final Map<Interval, Map<String, ClockSignalConsumer>> intervalToConsumersMap = new ConcurrentHashMap<>();
    private final ExecutorService taskExecutor;

    public void subscribe(Set<Interval> clockSignalIntervals, ClockSignalConsumer consumer) {
        clockSignalIntervals.forEach(clockSignalInterval -> {
            var consumers = intervalToConsumersMap.get(clockSignalInterval);
            if (isNull(consumers)) {
                consumers = new ConcurrentHashMap<>();
                intervalToConsumersMap.put(clockSignalInterval, consumers);
            }
            consumers.put(consumer.getId(), consumer);
        });
    }

    public void unsubscribe(Set<Interval> clockSignalIntervals, ClockSignalConsumer consumer) {
        clockSignalIntervals.forEach(clockSignalInterval ->
            Optional.ofNullable(intervalToConsumersMap.get(clockSignalInterval))
                .ifPresent(consumers -> consumers.remove(consumer.getId())));
    }

    public void dispatch(ClockSignal clockSignal) {
        Optional.ofNullable(intervalToConsumersMap.get(clockSignal.getInterval())).ifPresent(consumers ->
            consumers.values().forEach(consumer -> taskExecutor.submit(new ClockSignalDispatchTask(consumer, clockSignal))));
    }
}
