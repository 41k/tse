package root.tse.domain.chain_exchange_execution

import spock.lang.Specification

import static root.tse.util.TestUtils.CHAIN_EXCHANGE_EXECUTION_CONTEXT
import static root.tse.util.TestUtils.CHAIN_SYMBOLS

class ChainExchangeExecutionContextTest extends Specification {

    def 'should provide symbols'() {
        expect:
        CHAIN_EXCHANGE_EXECUTION_CONTEXT.getSymbols() == CHAIN_SYMBOLS
    }
}
