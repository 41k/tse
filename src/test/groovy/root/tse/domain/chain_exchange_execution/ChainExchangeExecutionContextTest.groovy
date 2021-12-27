package root.tse.domain.chain_exchange_execution

import spock.lang.Specification

import static root.tse.util.TestUtils.*

class ChainExchangeExecutionContextTest extends Specification {

    def 'should asset chain as string'() {
        expect:
        CHAIN_EXCHANGE_EXECUTION_CONTEXT.getAssetChainAsString() == ASSET_CHAIN_AS_STRING
    }

    def 'should provide symbols'() {
        expect:
        CHAIN_EXCHANGE_EXECUTION_CONTEXT.getSymbols() == CHAIN_SYMBOLS
    }
}
