package root.tse.domain.strategy_execution.clock

import root.tse.domain.strategy_execution.Interval
import spock.lang.Specification
import spock.lang.Unroll

class ClockSignalTest extends Specification {

    def 'should be properly initialized'() {
        given:
        def interval = Interval.FIFTEEN_MINUTES
        def timestampInMillis = 120125L
        def timestampInMillisRoundedToMinutes = 120000L

        when:
        def clockSignal = new ClockSignal(interval, timestampInMillis)

        then:
        clockSignal.interval == interval
        clockSignal.timestamp == timestampInMillisRoundedToMinutes
    }

    @Unroll
    def 'should check if clock signal is before provided clock signal'() {
        given:
        def clockSignal1 = new ClockSignal(Interval.ONE_MINUTE, timestamp1)
        def clockSignal2 = new ClockSignal(Interval.ONE_MINUTE, timestamp2)

        expect:
        clockSignal1.isBefore(clockSignal2) == result

        where:
        timestamp1     | timestamp2     || result
        1541421060000L | 1541421334000L || true
        1541421096000L | 1541421096000L || false
        1541421060000L | 1541421096000L || false
        1541421334000L | 1541421060000L || false
    }
}
