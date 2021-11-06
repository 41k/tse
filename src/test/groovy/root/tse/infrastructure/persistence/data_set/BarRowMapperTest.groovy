package root.tse.infrastructure.persistence.data_set

import spock.lang.Specification

import java.sql.ResultSet

class BarRowMapperTest extends Specification {

    private static final TIMESTAMP = 1639226400000L
    private static final OPEN_PRICE = 1000d
    private static final HIGH_PRICE = 1020d
    private static final LOW_PRICE = 990d
    private static final CLOSE_PRICE = 1010d
    private static final VOLUME = 100d

    private resultSet = Mock(ResultSet)
    private mapper = new BarRowMapper()

    def 'should map row to bar'() {
        when:
        def bar = mapper.mapRow(resultSet, 0)

        then:
        1 * resultSet.getLong('timestamp') >> TIMESTAMP
        1 * resultSet.getDouble('open') >> OPEN_PRICE
        1 * resultSet.getDouble('high') >> HIGH_PRICE
        1 * resultSet.getDouble('low') >> LOW_PRICE
        1 * resultSet.getDouble('close') >> CLOSE_PRICE
        1 * resultSet.getDouble('volume') >> VOLUME
        0 * _

        and:
        bar.getEndTime().toInstant().toEpochMilli() == TIMESTAMP
        bar.getOpenPrice().doubleValue() == OPEN_PRICE
        bar.getHighPrice().doubleValue() == HIGH_PRICE
        bar.getLowPrice().doubleValue() == LOW_PRICE
        bar.getClosePrice().doubleValue() == CLOSE_PRICE
        bar.getVolume().doubleValue() == VOLUME
    }
}
