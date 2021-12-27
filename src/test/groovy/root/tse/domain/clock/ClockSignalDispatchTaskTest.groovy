package root.tse.domain.clock

import spock.lang.Specification

import static root.tse.util.TestUtils.CLOCK_SIGNAL_1

class ClockSignalDispatchTaskTest extends Specification {

    def 'should dispatch clock signal to provided consumer'() {
        given:
        def clockSignalConsumer = Mock(ClockSignalConsumer)
        def task = new ClockSignalDispatchTask(clockSignalConsumer, CLOCK_SIGNAL_1)

        when:
        task.run()

        then:
        1 * clockSignalConsumer.accept(CLOCK_SIGNAL_1)
        0 * _
    }
}
