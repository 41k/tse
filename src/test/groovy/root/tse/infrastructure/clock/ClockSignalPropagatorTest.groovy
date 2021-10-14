package root.tse.infrastructure.clock

import root.tse.domain.strategy_execution.Interval
import root.tse.domain.strategy_execution.clock.ClockSignal
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

import static root.tse.domain.strategy_execution.Interval.*

class ClockSignalPropagatorTest extends Specification {

    private static final CLOCK_SIGNAL_PROPAGATION_TIMESTAMP = 1572850882000L

    private clock = Clock.fixed(Instant.ofEpochMilli(CLOCK_SIGNAL_PROPAGATION_TIMESTAMP), ZoneId.systemDefault())
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private clockSignalPropagator = new ClockSignalPropagator(clock, clockSignalDispatcher)

    def 'should propagate clock signal correctly'() {
        when:
        clockSignalPropagator.propagateOneMinuteSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(ONE_MINUTE))
        0 * _

        when:
        clockSignalPropagator.propagateThreeMinutesSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(THREE_MINUTES))
        0 * _

        when:
        clockSignalPropagator.propagateFiveMinutesSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(FIVE_MINUTES))
        0 * _

        when:
        clockSignalPropagator.propagateFifteenMinutesSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(FIFTEEN_MINUTES))
        0 * _

        when:
        clockSignalPropagator.propagateThirtyMinutesSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(THIRTY_MINUTES))
        0 * _

        when:
        clockSignalPropagator.propagateOneHourSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(ONE_HOUR))
        0 * _

        when:
        clockSignalPropagator.propagateTwoHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(TWO_HOURS))
        0 * _

        when:
        clockSignalPropagator.propagateFourHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(FOUR_HOURS))
        0 * _

        when:
        clockSignalPropagator.propagateSixHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(SIX_HOURS))
        0 * _

        when:
        clockSignalPropagator.propagateEightHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(EIGHT_HOURS))
        0 * _

        when:
        clockSignalPropagator.propagateTwelveHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(TWELVE_HOURS))
        0 * _

        when:
        clockSignalPropagator.propagateOneDaySignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(ONE_DAY))
        0 * _

        when:
        clockSignalPropagator.propagateThreeDaysSignal()

        then:
        1 * clockSignalDispatcher.dispatch(clockSignal(THREE_DAYS))
        0 * _
    }

    private static ClockSignal clockSignal(Interval clockSignalInterval) {
        new ClockSignal(clockSignalInterval, CLOCK_SIGNAL_PROPAGATION_TIMESTAMP)
    }
}
