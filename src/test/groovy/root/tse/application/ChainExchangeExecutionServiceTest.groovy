package root.tse.application

import root.tse.domain.chain_exchange_execution.ChainExchangeExecution
import root.tse.domain.chain_exchange_execution.ChainExchangeExecutionFactory
import spock.lang.Specification

import static root.tse.util.TestUtils.*

class ChainExchangeExecutionServiceTest extends Specification {

    private chainExchangeExecution1 = Mock(ChainExchangeExecution)
    private chainExchangeExecution2 = Mock(ChainExchangeExecution)
    private chainExchangeExecutionFactory = Mock(ChainExchangeExecutionFactory)
    private chainExchangeExecutionStore = [:]

    private chainExchangeExecutionService = new ChainExchangeExecutionService(
        CHAIN_EXCHANGE_EXECUTION_SETTINGS, chainExchangeExecutionFactory, chainExchangeExecutionStore)

    def setup() {
        chainExchangeExecutionStore.clear()
    }

    def 'should start chain exchange execution'() {
        given: 'no active chain exchange executions for now'
        assert chainExchangeExecutionStore.isEmpty()

        when:
        chainExchangeExecutionService.handle(START_CHAIN_EXCHANGE_EXECUTION_COMMAND)

        then:
        1 * chainExchangeExecutionFactory.create(CHAIN_EXCHANGE_EXECUTION_CONTEXT) >> chainExchangeExecution1
        0 * _

        and:
        chainExchangeExecutionStore.size() == 1
        chainExchangeExecutionStore.get(ASSET_CHAIN_ID) == chainExchangeExecution1
    }

    def 'should not start chain exchange execution if execution with the same asset chain is currently executed'() {
        given:
        chainExchangeExecutionStore.put(ASSET_CHAIN_ID, chainExchangeExecution1)
        assert chainExchangeExecutionStore.size() == 1

        when:
        chainExchangeExecutionService.handle(START_CHAIN_EXCHANGE_EXECUTION_COMMAND)

        then:
        0 * _

        and:
        chainExchangeExecutionStore.size() == 1
        chainExchangeExecutionStore.get(ASSET_CHAIN_ID) == chainExchangeExecution1
    }

    def 'should stop chain exchange execution'() {
        given:
        chainExchangeExecutionStore.put(ASSET_CHAIN_ID, chainExchangeExecution1)
        chainExchangeExecutionStore.put(2, chainExchangeExecution2)
        assert chainExchangeExecutionStore.size() == 2

        when:
        chainExchangeExecutionService.handle(STOP_CHAIN_EXCHANGE_EXECUTION_COMMAND)

        then:
        0 * _

        and:
        chainExchangeExecutionStore.size() == 1
        chainExchangeExecutionStore.get(2) == chainExchangeExecution2
    }
}
