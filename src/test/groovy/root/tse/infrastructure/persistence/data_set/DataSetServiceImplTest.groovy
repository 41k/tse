package root.tse.infrastructure.persistence.data_set

import org.springframework.jdbc.core.JdbcTemplate
import org.ta4j.core.Bar
import org.ta4j.core.num.PrecisionNum
import root.tse.domain.clock.Interval
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.*

class DataSetServiceImplTest extends Specification {

    private jdbcTemplate = Mock(JdbcTemplate)
    private barRowMapper = Mock(BarRowMapper)

    private DataSetServiceImpl dataSetService

    def setup() {
        dataSetService = new DataSetServiceImpl(jdbcTemplate, barRowMapper)
    }

    def 'should provide start timestamp'() {
        given:
        def expectedQuery = "SELECT MIN(timestamp) FROM $DATA_SET_NAME WHERE symbol='$SYMBOL_1'" as String

        when:
        def startTimestamp = dataSetService.getStartTimestamp(DATA_SET_NAME, SYMBOL_1)

        then:
        1 * jdbcTemplate.queryForObject(expectedQuery, Long) >> TIMESTAMP_1
        0 * _

        and:
        startTimestamp == TIMESTAMP_1
    }

    def 'should provide end timestamp'() {
        given:
        def expectedQuery = "SELECT MAX(timestamp) FROM $DATA_SET_NAME WHERE symbol='$SYMBOL_1'" as String

        when:
        def endTimestamp = dataSetService.getEndTimestamp(DATA_SET_NAME, SYMBOL_1)

        then:
        1 * jdbcTemplate.queryForObject(expectedQuery, Long) >> TIMESTAMP_2
        0 * _

        and:
        endTimestamp == TIMESTAMP_2
    }

    def 'should provide series'() {
        given:
        def interval = Interval.FIVE_MINUTES
        def expectedQuery = "SELECT * FROM $DATA_SET_NAME WHERE symbol = '$SYMBOL_1' AND time_interval = '$interval' ORDER BY id DESC"
        def untilTimestamp = 1639300500000L // 2021-12-12 09:15
        def seriesLength = 3
        def bar1 = Mock(Bar) // closed at 2021-12-12 09:00
        def bar1CloseTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1639299600000L), ZoneId.systemDefault())
        def bar2 = Mock(Bar) // closed at 2021-12-12 09:05
        def bar2CloseTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1639299900000L), ZoneId.systemDefault())
        def bar3 = Mock(Bar) // closed at 2021-12-12 09:10
        def bar3CloseTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1639300200000L), ZoneId.systemDefault())
        def bar4 = Mock(Bar) // closed at 2021-12-12 09:15
        def bar4CloseTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(1639300500000L), ZoneId.systemDefault())

        when:
        def series = dataSetService.getSeries(DATA_SET_NAME, SYMBOL_1, interval, untilTimestamp, seriesLength)

        then:
        1 * jdbcTemplate.query(expectedQuery, barRowMapper) >> [bar4, bar3, bar2, bar1]
        _ * bar1.getClosePrice() >> PrecisionNum.valueOf(1)
        _ * bar1.getEndTime() >> bar1CloseTime
        _ * bar2.getClosePrice() >> PrecisionNum.valueOf(1)
        _ * bar2.getEndTime() >> bar2CloseTime
        _ * bar3.getClosePrice() >> PrecisionNum.valueOf(1)
        _ * bar3.getEndTime() >> bar3CloseTime
        _ * bar4.getClosePrice() >> PrecisionNum.valueOf(1)
        _ * bar4.getEndTime() >> bar4CloseTime
        0 * _

        and:
        series.getBarData() == [bar2, bar3, bar4]
    }

    def 'should provide current prices'() {
        given:
        assert dataSetService.dataSetStore.isEmpty()

        and:
        def bar1 = Mock(Bar)
        def bar2 = Mock(Bar)
        dataSetService.dataSetStore.put(DATA_SET_NAME, [
            (SYMBOL_1) : [(Interval.ONE_MINUTE) : [(TIMESTAMP_1) : bar1]],
            (SYMBOL_2) : [(Interval.ONE_MINUTE) : [(TIMESTAMP_1) : bar2]]
        ])

        and:
        1 * bar1.getClosePrice() >> PrecisionNum.valueOf(PRICE_1)
        1 * bar2.getClosePrice() >> PrecisionNum.valueOf(PRICE_2)
        0 * _

        expect: 'current prices are provided'
        dataSetService.getCurrentPrices(DATA_SET_NAME, [SYMBOL_1, SYMBOL_2], TIMESTAMP_1) == Optional.of([
            (SYMBOL_1) : [(BUY) : PRICE_1, (SELL) : PRICE_1],
            (SYMBOL_2) : [(BUY) : PRICE_2, (SELL) : PRICE_2]
        ])
    }

    def 'should not return current prices if there are no prices for at least one symbol'() {
        given:
        assert dataSetService.dataSetStore.isEmpty()

        and:
        def expectedQuery = "SELECT * FROM $DATA_SET_NAME WHERE symbol = '$SYMBOL_1' AND time_interval = 'ONE_MINUTE' ORDER BY id DESC"

        and:
        1 * jdbcTemplate.query(expectedQuery, barRowMapper) >> []
        0 * _

        expect:
        dataSetService.getCurrentPrices(DATA_SET_NAME, [SYMBOL_1, SYMBOL_2], TIMESTAMP_1) == Optional.empty()
    }
}
