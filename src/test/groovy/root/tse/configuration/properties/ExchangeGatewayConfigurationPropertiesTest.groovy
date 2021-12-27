package root.tse.configuration.properties

import spock.lang.Specification

import static root.tse.domain.clock.Interval.ONE_DAY
import static root.tse.domain.clock.Interval.ONE_HOUR
import static root.tse.util.TestUtils.*

class ExchangeGatewayConfigurationPropertiesTest extends Specification {

    private static final INTERVAL_REPRESENTATION = '1d'
    private static final INTERVAL_TO_REPRESENTATION_MAP = [(ONE_DAY) : INTERVAL_REPRESENTATION]

    private configurationProperties =
        new ExchangeGatewayConfigurationProperties(
            intervalToRepresentationMap: INTERVAL_TO_REPRESENTATION_MAP,
            symbolSettings: SYMBOL_SETTINGS,
            orderFeePercent: ORDER_FEE_PERCENT,
            assetCodeDelimiter: ASSET_CODE_DELIMITER,
            assetChains: ASSET_CHAINS,
            numberOfAmountSelectionSteps: N_AMOUNT_SELECTION_STEPS
        )

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

    def 'should provide symbol names'() {
        expect:
        configurationProperties.getSymbolNames() == CHAIN_SYMBOLS
    }

    def 'should provide symbol to precision map'() {
        expect:
        configurationProperties.getSymbolToPrecisionMap() == SYMBOL_TO_PRECISION_MAP
    }

    def 'should provide chain exchange execution settings'() {
        expect:
        configurationProperties.getChainExchangeExecutionSettings() == CHAIN_EXCHANGE_EXECUTION_SETTINGS
    }
}
