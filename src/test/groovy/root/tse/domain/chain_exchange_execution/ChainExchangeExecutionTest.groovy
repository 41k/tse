package root.tse.domain.chain_exchange_execution

import root.tse.domain.event.EventBus
import spock.lang.Specification

import static root.tse.util.TestUtils.*

class ChainExchangeExecutionTest extends Specification {

    private chainExchangeService = Mock(ChainExchangeService)
    private eventBus = Mock(EventBus)

    private chainExchangeExecution = new ChainExchangeExecution(
        CHAIN_EXCHANGE_EXECUTION_CONTEXT, chainExchangeService, eventBus)

    def 'should execute chain exchange successfully'() {
        when:
        chainExchangeExecution.run()

        then:
        1 * chainExchangeService.tryToFormExpectedChainExchange(CHAIN_EXCHANGE_EXECUTION_CONTEXT) >> Optional.of(EXPECTED_CHAIN_EXCHANGE)
        1 * chainExchangeService.tryToExecute(EXPECTED_CHAIN_EXCHANGE, CHAIN_EXCHANGE_EXECUTION_CONTEXT) >> Optional.of(EXECUTED_CHAIN_EXCHANGE)
        1 * eventBus.publishChainExchangeWasExecutedEvent(EXECUTED_CHAIN_EXCHANGE, ASSET_CHAIN)
        0 * _
    }

    def 'should not execute chain exchange if it is not possible to form expected chain exchange'() {
        when:
        chainExchangeExecution.run()

        then:
        1 * chainExchangeService.tryToFormExpectedChainExchange(CHAIN_EXCHANGE_EXECUTION_CONTEXT) >> Optional.empty()
        0 * _
    }

    def 'should not execute chain exchange if expected profit is lower than threshold'() {
        given:
        def expectedChainExchange = EXPECTED_CHAIN_EXCHANGE.toBuilder().profit(MIN_PROFIT_THRESHOLD - 1).build()

        when:
        chainExchangeExecution.run()

        then:
        1 * chainExchangeService.tryToFormExpectedChainExchange(CHAIN_EXCHANGE_EXECUTION_CONTEXT) >> Optional.of(expectedChainExchange)
        0 * _
    }

    def 'should publish appropriate event if chain exchange execution failed'() {
        when:
        chainExchangeExecution.run()

        then:
        1 * chainExchangeService.tryToFormExpectedChainExchange(CHAIN_EXCHANGE_EXECUTION_CONTEXT) >> Optional.of(EXPECTED_CHAIN_EXCHANGE)
        1 * chainExchangeService.tryToExecute(EXPECTED_CHAIN_EXCHANGE, CHAIN_EXCHANGE_EXECUTION_CONTEXT) >> Optional.empty()
        1 * eventBus.publishChainExchangeExecutionFailedEvent(CHAIN_EXCHANGE_ID, ASSET_CHAIN)
        0 * _
    }
}
