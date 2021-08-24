package root.tse.domain.strategy_execution

import org.ta4j.core.Bar
import org.ta4j.core.BaseBar
import org.ta4j.core.BaseBarSeries
import spock.lang.Specification

import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

class BarSeriesTest extends Specification {

    def 'series should be correct'() {
        given:
        def seriesLength = 10
        def expectedSeriesEndIndex = seriesLength - 1
        def expectedLastBarPrice = seriesLength as double

        and:
        def bars = [] as List
        1.upto(seriesLength, { bars << bar(it as double) })
        def series = new BaseBarSeries(bars)

        expect:
        series.getBarCount() == seriesLength

        and: 'order of bars is correct'
        1.upto(seriesLength, {
            def expectedPrice = it as double
            def index = (it - 1) as int
            def bar = series.getBar(index)
            def price = bar.getClosePrice().doubleValue()
            assert price == expectedPrice
        })

        and: 'end index is correct'
        def endIndex = series.getEndIndex()
        assert expectedSeriesEndIndex == endIndex

        and: 'last bar price is correct'
        assert expectedLastBarPrice == series.getBar(endIndex).getClosePrice().doubleValue()
        assert expectedLastBarPrice == series.getLastBar().getClosePrice().doubleValue()
    }

    private static Bar bar(double price) {
        def barTime = ZonedDateTime.ofInstant(Instant.now(), ZoneId.systemDefault())
        def priceAsString = price as String
        return new BaseBar(Duration.ofMinutes(1), barTime, priceAsString, priceAsString, priceAsString, priceAsString, priceAsString)
    }
}
