package root.tse.infrastructure.clock;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher;

import static root.tse.domain.strategy_execution.Interval.*;

// Note: all signals are shifted on 5 minutes in order to avoid data update delays on exchange gateway's side
@Slf4j
@RequiredArgsConstructor
public class ClockSignalPropagator {

    private static final String SIGNAL_HAS_BEEN_PROPAGATED = ">>> clock signal for {} interval has been propagated";

    private final ClockSignalDispatcher clockSignalDispatcher;

    @Scheduled(cron = "5 0/1 * * * *")
    public void propagateOneMinuteSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, ONE_MINUTE);
        clockSignalDispatcher.dispatch(ONE_MINUTE);
    }

    @Scheduled(cron = "5 0/3 * * * *")
    public void propagateThreeMinutesSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, THREE_MINUTES);
        clockSignalDispatcher.dispatch(THREE_MINUTES);
    }

    @Scheduled(cron = "5 0/5 * * * *")
    public void propagateFiveMinutesSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, FIVE_MINUTES);
        clockSignalDispatcher.dispatch(FIVE_MINUTES);
    }

    @Scheduled(cron = "5 0/15 * * * *")
    public void propagateFifteenMinutesSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, FIFTEEN_MINUTES);
        clockSignalDispatcher.dispatch(FIFTEEN_MINUTES);
    }

    @Scheduled(cron = "5 0/30 * * * *")
    public void propagateThirtyMinutesSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, THIRTY_MINUTES);
        clockSignalDispatcher.dispatch(THIRTY_MINUTES);
    }

    @Scheduled(cron = "5 0 0/1 * * *")
    public void propagateOneHourSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, ONE_HOUR);
        clockSignalDispatcher.dispatch(ONE_HOUR);
    }

    @Scheduled(cron = "5 0 0/2 * * *")
    public void propagateTwoHoursSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, TWO_HOURS);
        clockSignalDispatcher.dispatch(TWO_HOURS);
    }

    @Scheduled(cron = "5 0 0/4 * * *")
    public void propagateFourHoursSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, FOUR_HOURS);
        clockSignalDispatcher.dispatch(FOUR_HOURS);
    }

    @Scheduled(cron = "5 0 0/6 * * *")
    public void propagateSixHoursSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, SIX_HOURS);
        clockSignalDispatcher.dispatch(SIX_HOURS);
    }

    @Scheduled(cron = "5 0 0/8 * * *")
    public void propagateEightHoursSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, EIGHT_HOURS);
        clockSignalDispatcher.dispatch(EIGHT_HOURS);
    }

    @Scheduled(cron = "5 0 0/12 * * *")
    public void propagateTwelveHoursSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, TWELVE_HOURS);
        clockSignalDispatcher.dispatch(TWELVE_HOURS);
    }

    @Scheduled(cron = "5 0 0 0/1 * *")
    public void propagateOneDaySignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, ONE_DAY);
        clockSignalDispatcher.dispatch(ONE_DAY);
    }

    @Scheduled(cron = "5 0 0 0/3 * *")
    public void propagateThreeDaysSignal() {
        log.debug(SIGNAL_HAS_BEEN_PROPAGATED, THREE_DAYS);
        clockSignalDispatcher.dispatch(THREE_DAYS);
    }
}
