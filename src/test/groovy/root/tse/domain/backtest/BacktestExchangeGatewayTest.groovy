package root.tse.domain.backtest

import org.ta4j.core.BarSeries
import root.tse.configuration.properties.BacktestConfigurationProperties
import root.tse.domain.clock.Interval
import root.tse.domain.order.Order
import spock.lang.Specification

import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.*

class BacktestExchangeGatewayTest extends Specification {

    private static final CURRENT_PRICES = Optional.of([
        (SYMBOL_1) : [(BUY) : PRICE_1, (SELL) : PRICE_1],
        (SYMBOL_2) : [(BUY) : PRICE_2, (SELL) : PRICE_2]
    ])

    private backtestProperties = new BacktestConfigurationProperties(
        dataSetName: DATA_SET_NAME,
        symbol: SYMBOL_1,
        orderFeePercent: ORDER_FEE_PERCENT
    )
    private dataSetService = Mock(DataSetService)
    private backtestExchangeGateway = new BacktestExchangeGateway(backtestProperties, dataSetService)

    def 'should provide order fee percent'() {
        expect:
        backtestExchangeGateway.getOrderFeePercent() == ORDER_FEE_PERCENT
    }

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

    def 'should provide current prices'() {
        given:
        def symbols = [SYMBOL_1, SYMBOL_2]
        def currentTimestamp = TIMESTAMP_1

        and:
        backtestExchangeGateway.setCurrentTimestamp(currentTimestamp)

        and:
        1 * dataSetService.getCurrentPrices(DATA_SET_NAME, symbols, currentTimestamp) >> CURRENT_PRICES
        0 * _

        expect:
        backtestExchangeGateway.getCurrentPrices(symbols) == CURRENT_PRICES
    }

    def 'should execute order successfully'() {
        given:
        def order = Order.builder().type(BUY).symbol(SYMBOL_1).build()

        and:
        def symbols = List.of(SYMBOL_1)
        def currentTimestamp = TIMESTAMP_1
        backtestExchangeGateway.setCurrentTimestamp(currentTimestamp)

        and:
        1 * dataSetService.getCurrentPrices(DATA_SET_NAME, symbols, currentTimestamp) >> CURRENT_PRICES
        0 * _

        when:
        def executedOrder = backtestExchangeGateway.tryToExecute(order).get()

        then:
        executedOrder.getPrice() == PRICE_1
    }

    def 'should not execute order if current prices were not obtained'() {
        given:
        def order = Order.builder().type(BUY).symbol(SYMBOL_1).build()

        and:
        def symbols = List.of(SYMBOL_1)
        def currentTimestamp = TIMESTAMP_1
        backtestExchangeGateway.setCurrentTimestamp(currentTimestamp)

        and:
        1 * dataSetService.getCurrentPrices(DATA_SET_NAME, symbols, currentTimestamp) >> Optional.empty()
        0 * _

        expect:
        backtestExchangeGateway.tryToExecute(order).isEmpty()
    }

    def 'should provide start timestamp'() {
        when:
        def startTimestamp = backtestExchangeGateway.getStartTimestamp()

        then:
        1 * dataSetService.getStartTimestamp(DATA_SET_NAME, SYMBOL_1) >> TIMESTAMP_1
        0 * _

        and:
        startTimestamp == TIMESTAMP_1
    }

    def 'should provide end timestamp'() {
        when:
        def endTimestamp = backtestExchangeGateway.getEndTimestamp()

        then:
        1 * dataSetService.getEndTimestamp(DATA_SET_NAME, SYMBOL_1) >> TIMESTAMP_2
        0 * _

        and:
        endTimestamp == TIMESTAMP_2
    }
}
