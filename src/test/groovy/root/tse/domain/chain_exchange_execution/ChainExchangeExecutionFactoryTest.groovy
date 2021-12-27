package root.tse.domain.chain_exchange_execution

import root.tse.domain.ExchangeGateway
import root.tse.domain.event.EventBus
import spock.lang.Specification

import static root.tse.util.TestUtils.CHAIN_EXCHANGE_EXECUTION_CONTEXT

class ChainExchangeExecutionFactoryTest extends Specification {

    private exchangeGateway = Mock(ExchangeGateway)
    private chainExchangeService = Mock(ChainExchangeService)
    private eventBus = Mock(EventBus)

    private chainExchangeExecutionFactory =
        new ChainExchangeExecutionFactory(exchangeGateway, chainExchangeService, eventBus)

    def 'should create chain exchange execution correctly'() {
        when:
        def chainExchangeExecution = chainExchangeExecutionFactory.create(CHAIN_EXCHANGE_EXECUTION_CONTEXT)

        then:
        0 * _

        and:
        chainExchangeExecution.context == CHAIN_EXCHANGE_EXECUTION_CONTEXT
        chainExchangeExecution.exchangeGateway == exchangeGateway
        chainExchangeExecution.chainExchangeService == chainExchangeService
        chainExchangeExecution.eventBus == eventBus
    }
}
