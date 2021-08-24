package root.tse.domain.strategy_execution.clock

import spock.lang.Specification

class ClockSignalDispatchTaskTest extends Specification {

    def 'should dispatch clock signal to provided consumer'() {
        given:
        def clockSignalConsumer = Mock(ClockSignalConsumer)
        def task = new ClockSignalDispatchTask(clockSignalConsumer)

        when:
        task.run()

        then:
        1 * clockSignalConsumer.acceptClockSignal()
        0 * _
    }
}
