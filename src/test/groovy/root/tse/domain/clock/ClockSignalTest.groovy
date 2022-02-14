package root.tse.domain.clock

import spock.lang.Specification
import spock.lang.Unroll

import static root.tse.domain.clock.Interval.*

class ClockSignalTest extends Specification {

    @Unroll
    def 'should be properly initialized'() {
        when:
        def clockSignal = new ClockSignal(interval, 1645115116125L)

        then:
        clockSignal.interval == interval
        clockSignal.timestamp == timestamp

        where:
        interval        || timestamp
        ONE_SECOND      || 1645115116000L
        ONE_MINUTE      || 1645115100000L
        THREE_MINUTES   || 1645115040000L
        FIVE_MINUTES    || 1645115100000L
        FIFTEEN_MINUTES || 1645114500000L
        THIRTY_MINUTES  || 1645113600000L
        ONE_HOUR        || 1645113600000L
        TWO_HOURS       || 1645113600000L
        FOUR_HOURS      || 1645113600000L
        SIX_HOURS       || 1645099200000L
        EIGHT_HOURS     || 1645113600000L
        TWELVE_HOURS    || 1645099200000L
        ONE_DAY         || 1645056000000L
        THREE_DAYS      || 1644883200000L
    }

    @Unroll
    def 'should check if clock signal is before provided clock signal'() {
        given:
        def clockSignal1 = new ClockSignal(ONE_MINUTE, timestamp1)
        def clockSignal2 = new ClockSignal(ONE_MINUTE, timestamp2)

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
