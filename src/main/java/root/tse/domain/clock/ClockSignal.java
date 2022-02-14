package root.tse.domain.clock;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

@Getter
@EqualsAndHashCode
@ToString
public class ClockSignal {

    private final Interval interval;
    private final Long timestamp;

    public ClockSignal(Interval interval, Long timestamp) {
        this.interval = interval;
        this.timestamp = roundToInterval(timestamp, interval);
    }

    public boolean isBefore(ClockSignal clockSignal) {
        return timestamp < clockSignal.getTimestamp();
    }

    private Long roundToInterval(Long timestamp, Interval interval) {
        return timestamp - (timestamp % interval.inMillis());
    }
}
