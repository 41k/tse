package root.tse.domain.backtest

import org.ta4j.core.BarSeries
import root.tse.domain.strategy_execution.Interval
import root.tse.domain.strategy_execution.trade.Order
import spock.lang.Specification

import static root.tse.util.TestUtils.*
import static root.tse.domain.strategy_execution.trade.OrderStatus.FILLED
import static root.tse.domain.strategy_execution.trade.OrderStatus.NEW

class BacktestExchangeGatewayTest extends Specification {

    private dataSetService = Mock(DataSetService)
    private backtestExchangeGateway = new BacktestExchangeGateway(dataSetService, DATA_SET_NAME)

    def 'should provide series'() {
        given:
        def interval = Interval.THREE_MINUTES
        def currentTimestamp = TIMESTAMP_1
        def seriesLength = 100
        def expectedSeries = Mock(BarSeries)

        and:
        backtestExchangeGateway.setCurrentTimestamp(currentTimestamp)

        when:
        def series = backtestExchangeGateway.getSeries(SYMBOL_1, interval, seriesLength)

        then:
        1 * dataSetService.getSeries(DATA_SET_NAME, SYMBOL_1, interval, currentTimestamp, seriesLength) >> expectedSeries
        0 * _

        and:
        series.get() == expectedSeries
    }

    def 'should execute order by just changing status to FILLED'() {
        given:
        def order = Order.builder().build()

        and:
        assert order.getStatus() == NEW

        when:
        def executedOrder = backtestExchangeGateway.execute(order)

        then:
        executedOrder.getStatus() == FILLED
    }

    def 'should provide start timestamp'() {
        when:
        def startTimestamp = backtestExchangeGateway.getStartTimestamp(SYMBOL_1)

        then:
        1 * dataSetService.getStartTimestamp(DATA_SET_NAME, SYMBOL_1) >> TIMESTAMP_1
        0 * _

        and:
        startTimestamp == TIMESTAMP_1
    }

    def 'should provide end timestamp'() {
        when:
        def endTimestamp = backtestExchangeGateway.getEndTimestamp(SYMBOL_1)

        then:
        1 * dataSetService.getEndTimestamp(DATA_SET_NAME, SYMBOL_1) >> TIMESTAMP_2
        0 * _

        and:
        endTimestamp == TIMESTAMP_2
    }
}
