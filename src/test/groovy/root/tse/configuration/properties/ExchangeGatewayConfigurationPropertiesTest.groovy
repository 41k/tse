package root.tse.configuration.properties

import spock.lang.Specification

import static root.tse.domain.strategy_execution.Interval.ONE_DAY
import static root.tse.domain.strategy_execution.Interval.ONE_HOUR

class ExchangeGatewayConfigurationPropertiesTest extends Specification {

    private static final INTERVAL_REPRESENTATION = '1d'
    private static final INTERVAL_TO_REPRESENTATION_MAP = [(ONE_DAY) : INTERVAL_REPRESENTATION]

    private configurationProperties =
        new ExchangeGatewayConfigurationProperties(intervalToRepresentationMap: INTERVAL_TO_REPRESENTATION_MAP)

    def 'should return interval representation successfully'() {
        expect:
        configurationProperties.getIntervalRepresentation(ONE_DAY) == INTERVAL_REPRESENTATION
    }

    def 'should throw exception if representation is not configured for interval'() {
        when:
        configurationProperties.getIntervalRepresentation(ONE_HOUR)

        then:
        def exception = thrown(NoSuchElementException)
        exception.message == '>>> no representation configured for ONE_HOUR interval.'
    }
}
