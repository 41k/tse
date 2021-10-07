package root.tse.infrastructure.clock

import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import spock.lang.Specification

import static root.tse.domain.strategy_execution.Interval.*

class ClockSignalPropagatorTest extends Specification {

    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private clockSignalPropagator = new ClockSignalPropagator(clockSignalDispatcher)

    def 'should propagate clock signal correctly'() {
        when:
        clockSignalPropagator.propagateOneMinuteSignal()

        then:
        1 * clockSignalDispatcher.dispatch(ONE_MINUTE)
        0 * _

        when:
        clockSignalPropagator.propagateThreeMinutesSignal()

        then:
        1 * clockSignalDispatcher.dispatch(THREE_MINUTES)
        0 * _

        when:
        clockSignalPropagator.propagateFiveMinutesSignal()

        then:
        1 * clockSignalDispatcher.dispatch(FIVE_MINUTES)
        0 * _

        when:
        clockSignalPropagator.propagateFifteenMinutesSignal()

        then:
        1 * clockSignalDispatcher.dispatch(FIFTEEN_MINUTES)
        0 * _

        when:
        clockSignalPropagator.propagateThirtyMinutesSignal()

        then:
        1 * clockSignalDispatcher.dispatch(THIRTY_MINUTES)
        0 * _

        when:
        clockSignalPropagator.propagateOneHourSignal()

        then:
        1 * clockSignalDispatcher.dispatch(ONE_HOUR)
        0 * _

        when:
        clockSignalPropagator.propagateTwoHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(TWO_HOURS)
        0 * _

        when:
        clockSignalPropagator.propagateFourHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(FOUR_HOURS)
        0 * _

        when:
        clockSignalPropagator.propagateSixHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(SIX_HOURS)
        0 * _

        when:
        clockSignalPropagator.propagateEightHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(EIGHT_HOURS)
        0 * _

        when:
        clockSignalPropagator.propagateTwelveHoursSignal()

        then:
        1 * clockSignalDispatcher.dispatch(TWELVE_HOURS)
        0 * _

        when:
        clockSignalPropagator.propagateOneDaySignal()

        then:
        1 * clockSignalDispatcher.dispatch(ONE_DAY)
        0 * _

        when:
        clockSignalPropagator.propagateThreeDaysSignal()

        then:
        1 * clockSignalDispatcher.dispatch(THREE_DAYS)
        0 * _
    }
}
