package root.tse.infrastructure.clock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import root.tse.domain.clock.Interval;
import root.tse.domain.clock.ClockSignal;
import root.tse.domain.clock.ClockSignalDispatcher;

import java.time.Clock;

import static root.tse.domain.clock.Interval.*;

// Note: all signals are shifted on 15 seconds in order to avoid data update delays on exchange gateway's side
@Slf4j
@RequiredArgsConstructor
public class ClockSignalPropagator {

    private final Clock clock;
    private final ClockSignalDispatcher clockSignalDispatcher;

    @Scheduled(cron = "0/1 * * * * *")
    public void propagateOneSecondSignal() {
        propagateClockSignal(ONE_SECOND);
    }

    @Scheduled(cron = "15 0/1 * * * *")
    public void propagateOneMinuteSignal() {
        propagateClockSignal(ONE_MINUTE);
    }

    @Scheduled(cron = "15 0/3 * * * *")
    public void propagateThreeMinutesSignal() {
        propagateClockSignal(THREE_MINUTES);
    }

    @Scheduled(cron = "15 0/5 * * * *")
    public void propagateFiveMinutesSignal() {
        propagateClockSignal(FIVE_MINUTES);
    }

    @Scheduled(cron = "15 0/15 * * * *")
    public void propagateFifteenMinutesSignal() {
        propagateClockSignal(FIFTEEN_MINUTES);
    }

    @Scheduled(cron = "15 0/30 * * * *")
    public void propagateThirtyMinutesSignal() {
        propagateClockSignal(THIRTY_MINUTES);
    }

    @Scheduled(cron = "15 0 0/1 * * *")
    public void propagateOneHourSignal() {
        propagateClockSignal(ONE_HOUR);
    }

    @Scheduled(cron = "15 0 0/2 * * *")
    public void propagateTwoHoursSignal() {
        propagateClockSignal(TWO_HOURS);
    }

    @Scheduled(cron = "15 0 0/4 * * *")
    public void propagateFourHoursSignal() {
        propagateClockSignal(FOUR_HOURS);
    }

    @Scheduled(cron = "15 0 0/6 * * *")
    public void propagateSixHoursSignal() {
        propagateClockSignal(SIX_HOURS);
    }

    @Scheduled(cron = "15 0 0/8 * * *")
    public void propagateEightHoursSignal() {
        propagateClockSignal(EIGHT_HOURS);
    }

    @Scheduled(cron = "15 0 0/12 * * *")
    public void propagateTwelveHoursSignal() {
        propagateClockSignal(TWELVE_HOURS);
    }

    @Scheduled(cron = "15 0 0 0/1 * *")
    public void propagateOneDaySignal() {
        propagateClockSignal(ONE_DAY);
    }

    @Scheduled(cron = "15 0 0 0/3 * *")
    public void propagateThreeDaysSignal() {
        propagateClockSignal(THREE_DAYS);
    }

    private void propagateClockSignal(Interval clockSignalInterval) {
        var timestamp = clock.millis();
        var clockSignal = new ClockSignal(clockSignalInterval, timestamp);
        log.trace(">>> {} has been propagated", clockSignal);
        clockSignalDispatcher.dispatch(clockSignal);
    }
}
