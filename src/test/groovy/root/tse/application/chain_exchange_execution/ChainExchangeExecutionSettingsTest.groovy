package root.tse.application.chain_exchange_execution

import spock.lang.Specification

import static root.tse.util.TestUtils.*

class ChainExchangeExecutionSettingsTest extends Specification {

    def 'should provide asset chain'() {
        expect:
        CHAIN_EXCHANGE_EXECUTION_SETTINGS.getAssetChain(ASSET_CHAIN_ID) == ASSET_CHAIN
    }

    def 'should throw exception if asset chain is not found by id'() {
        given:
        def assetChainId = 2

        when:
        CHAIN_EXCHANGE_EXECUTION_SETTINGS.getAssetChain(assetChainId)

        then:
        def exception = thrown(IllegalArgumentException)
        exception.message == "Asset chain with id [$assetChainId] is not configured"
    }
}
