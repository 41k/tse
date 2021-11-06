package root.tse.domain.strategy_execution.clock

import root.tse.domain.strategy_execution.Interval
import spock.lang.Specification

import java.util.concurrent.ExecutorService

import static root.tse.util.TestUtils.createClockSignal

class DefaultClockSignalDispatcherTest extends Specification {

    private static final INTERVAL_1 = Interval.ONE_MINUTE
    private static final INTERVAL_2 = Interval.ONE_HOUR

    private static final CONSUMER_ID_1 = 'consumer-1'
    private static final CONSUMER_ID_2 = 'consumer-2'
    private static final CONSUMER_ID_3 = 'consumer-3'

    private consumer1 = Mock(ClockSignalConsumer)
    private consumer2 = Mock(ClockSignalConsumer)
    private consumer3 = Mock(ClockSignalConsumer)

    private taskExecutor = Mock(ExecutorService)

    private ClockSignalDispatcher clockSignalDispatcher

    def setup() {
        clockSignalDispatcher = new DefaultClockSignalDispatcher(taskExecutor)
    }

    def 'should subscribe consumers correctly'() {
        given: 'dispatcher without consumers'
        assert clockSignalDispatcher.intervalToConsumersMap.size() == 0

        when:
        clockSignalDispatcher.subscribe([INTERVAL_1, INTERVAL_2] as Set, consumer1)

        and:
        clockSignalDispatcher.subscribe([INTERVAL_2] as Set, consumer2)
        clockSignalDispatcher.subscribe([INTERVAL_1, INTERVAL_2] as Set, consumer3)

        then:
        2 * consumer1.getId() >> CONSUMER_ID_1
        1 * consumer2.getId() >> CONSUMER_ID_2
        2 * consumer3.getId() >> CONSUMER_ID_3
        0 * _

        and:
        clockSignalDispatcher.intervalToConsumersMap == [
            (INTERVAL_1) : [
                (CONSUMER_ID_1) : consumer1,
                (CONSUMER_ID_3) : consumer3
            ],
            (INTERVAL_2) : [
                (CONSUMER_ID_1) : consumer1,
                (CONSUMER_ID_2) : consumer2,
                (CONSUMER_ID_3) : consumer3
            ]
        ]
    }

    def 'should unsubscribe consumers correctly'() {
        given: 'dispatcher with consumers'
        clockSignalDispatcher.intervalToConsumersMap << [
            (INTERVAL_1) : [
                (CONSUMER_ID_1) : consumer1,
                (CONSUMER_ID_3) : consumer3
            ],
            (INTERVAL_2) : [
                (CONSUMER_ID_3) : consumer3
            ]
        ]

        when:
        clockSignalDispatcher.unsubscribe([INTERVAL_1, INTERVAL_2] as Set, consumer3)

        then:
        2 * consumer3.getId() >> CONSUMER_ID_3
        0 * _

        and:
        clockSignalDispatcher.intervalToConsumersMap == [
            (INTERVAL_1) : [
                (CONSUMER_ID_1) : consumer1
            ],
            (INTERVAL_2) : [:]
        ]
    }

    def 'should dispatch clock signal to appropriate consumers'() {
        given: 'dispatcher with consumers'
        clockSignalDispatcher.intervalToConsumersMap << [
            (INTERVAL_1) : [
                (CONSUMER_ID_1) : consumer1,
                (CONSUMER_ID_3) : consumer3
            ],
            (INTERVAL_2) : [
                (CONSUMER_ID_2) : consumer2
            ]
        ]

        and:
        def clockSignal = createClockSignal(INTERVAL_1)

        when:
        clockSignalDispatcher.dispatch(clockSignal)

        then:
        1 * taskExecutor.submit(new ClockSignalDispatchTask(consumer1, clockSignal))
        1 * taskExecutor.submit(new ClockSignalDispatchTask(consumer3, clockSignal))
        0 * _
    }

    def 'should not dispatch clock signal if there is no consumer for clock signal interval'() {
        given: 'dispatcher with consumers'
        clockSignalDispatcher.intervalToConsumersMap << [
            (INTERVAL_2) : [
                (CONSUMER_ID_3) : consumer3
            ]
        ]

        and:
        def clockSignal = createClockSignal(INTERVAL_1)

        when:
        clockSignalDispatcher.dispatch(clockSignal)

        then:
        0 * _
    }
}
