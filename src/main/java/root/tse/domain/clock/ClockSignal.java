package root.tse.domain.clock;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import static root.tse.domain.clock.Interval.ONE_MINUTE;

@Getter
@EqualsAndHashCode
@ToString
public class ClockSignal {

    private final Interval interval;
    private final Long timestamp;

    public ClockSignal(Interval interval, Long timestamp) {
        this.interval = interval;
        this.timestamp = roundToMinutes(timestamp);
    }

    public boolean isBefore(ClockSignal clockSignal) {
        return timestamp < clockSignal.getTimestamp();
    }

    private Long roundToMinutes(Long timestamp) {
        return timestamp - (timestamp % ONE_MINUTE.inMillis());
    }
}
