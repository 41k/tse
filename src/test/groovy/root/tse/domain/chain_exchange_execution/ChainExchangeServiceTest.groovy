package root.tse.domain.chain_exchange_execution

import root.tse.domain.ExchangeGateway
import root.tse.domain.IdGenerator
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Clock

import static root.tse.util.TestUtils.*

class ChainExchangeServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private exchangeGateway = Mock(ExchangeGateway)
    private initialOrderAmountCalculator = Mock(InitialOrderAmountCalculator)
    private chainExchangeRepository = Mock(ChainExchangeRepository)
    private clock = Mock(Clock)
    private chainExchangeService = new ChainExchangeService(
        idGenerator, exchangeGateway, initialOrderAmountCalculator, chainExchangeRepository, clock)

    def 'should form expected chain exchange'() {
        when:
        def expectedChainExchange =
            chainExchangeService.tryToFormExpectedChainExchange(CHAIN_EXCHANGE_EXECUTION_CONTEXT).get()

        then:
        1 * exchangeGateway.getOrderFeePercent() >> ORDER_FEE_PERCENT
        1 * exchangeGateway.getCurrentPrices(CHAIN_SYMBOLS) >> Optional.of(CHAIN_PRICES)
        1 * initialOrderAmountCalculator.tryToCalculate(CHAIN_EXCHANGE_EXECUTION_CONTEXT, CHAIN_PRICES, ORDER_FEE_PERCENT) >> Optional.of(CHAIN_ORDER_1_AMOUNT)
        1 * idGenerator.generateId() >> CHAIN_EXCHANGE_ID
        1 * clock.millis() >> CHAIN_EXCHANGE_EXECUTION_TIMESTAMP
        0 * _

        and:
        expectedChainExchange == EXPECTED_CHAIN_EXCHANGE
    }

    def 'should return empty optional if current prices retrieval failed'() {
        given:
        1 * exchangeGateway.getOrderFeePercent() >> ORDER_FEE_PERCENT
        1 * exchangeGateway.getCurrentPrices(CHAIN_SYMBOLS) >> Optional.empty()
        0 * _

        expect:
        chainExchangeService.tryToFormExpectedChainExchange(CHAIN_EXCHANGE_EXECUTION_CONTEXT).isEmpty()
    }

    def 'should return empty optional if initial order amount calculation failed'() {
        given:
        1 * exchangeGateway.getOrderFeePercent() >> ORDER_FEE_PERCENT
        1 * exchangeGateway.getCurrentPrices(CHAIN_SYMBOLS) >> Optional.of(CHAIN_PRICES)
        1 * initialOrderAmountCalculator.tryToCalculate(CHAIN_EXCHANGE_EXECUTION_CONTEXT, CHAIN_PRICES, ORDER_FEE_PERCENT) >> Optional.empty()
        0 * _

        expect:
        chainExchangeService.tryToFormExpectedChainExchange(CHAIN_EXCHANGE_EXECUTION_CONTEXT).isEmpty()
    }

    def 'should execute chain exchange successfully'() {
        when:
        def result = chainExchangeService.tryToExecute(EXPECTED_CHAIN_EXCHANGE, CHAIN_EXCHANGE_EXECUTION_CONTEXT)

        then:
        1 * exchangeGateway.tryToExecute(CHAIN_ORDER_1) >> Optional.of(CHAIN_ORDER_1)
        1 * exchangeGateway.tryToExecute(CHAIN_ORDER_2) >> Optional.of(CHAIN_ORDER_2)
        1 * exchangeGateway.tryToExecute(CHAIN_ORDER_3) >> Optional.of(CHAIN_ORDER_3)
        1 * chainExchangeRepository.save(EXECUTED_CHAIN_EXCHANGE)
        0 * _

        and:
        result.get() == EXECUTED_CHAIN_EXCHANGE
    }

    @Unroll
    def 'should stop chain exchange execution if order#orderNumber execution failed'() {
        when:
        def result = chainExchangeService.tryToExecute(EXPECTED_CHAIN_EXCHANGE, CHAIN_EXCHANGE_EXECUTION_CONTEXT)

        then:
        _ * exchangeGateway.tryToExecute(CHAIN_ORDER_1) >> executedOrder1
        _ * exchangeGateway.tryToExecute(CHAIN_ORDER_2) >> executedOrder2
        _ * exchangeGateway.tryToExecute(CHAIN_ORDER_3) >> executedOrder3
        0 * _

        and:
        result.isEmpty()

        and:
        noExceptionThrown()

        where:
        orderNumber | executedOrder1             | executedOrder2             | executedOrder3
        1           | Optional.empty()           | _                          | _
        2           | Optional.of(CHAIN_ORDER_1) | Optional.empty()           | _
        3           | Optional.of(CHAIN_ORDER_1) | Optional.of(CHAIN_ORDER_2) | Optional.empty()
    }
}
