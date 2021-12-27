package root.tse.domain.chain_exchange_execution

import root.tse.domain.IdGenerator
import root.tse.domain.order.OrderExecutor
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Clock

import static root.tse.domain.order.OrderStatus.FILLED
import static root.tse.domain.order.OrderStatus.NOT_FILLED
import static root.tse.util.TestUtils.*

class ChainExchangeServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private orderExecutor = Mock(OrderExecutor)
    private initialOrderAmountCalculator = Mock(InitialOrderAmountCalculator)
    private chainExchangeRepository = Mock(ChainExchangeRepository)
    private clock = Mock(Clock)
    private chainExchangeService = new ChainExchangeService(
        idGenerator, orderExecutor, initialOrderAmountCalculator, chainExchangeRepository, clock)

    def 'should form expected chain exchange'() {
        when:
        def expectedChainExchange =
            chainExchangeService.tryToFormExpectedChainExchange(CHAIN_EXCHANGE_EXECUTION_CONTEXT, CHAIN_PRICES).get()

        then:
        1 * initialOrderAmountCalculator.tryToCalculate(CHAIN_EXCHANGE_EXECUTION_CONTEXT, CHAIN_PRICES) >> Optional.of(CHAIN_ORDER_1_AMOUNT)
        1 * idGenerator.generateId() >> CHAIN_EXCHANGE_ID
        1 * clock.millis() >> CHAIN_EXCHANGE_EXECUTION_TIMESTAMP
        0 * _

        and:
        expectedChainExchange == EXPECTED_CHAIN_EXCHANGE
    }

    def 'should return empty optional if initial order amount calculation failed'() {
        given:
        1 * initialOrderAmountCalculator.tryToCalculate(CHAIN_EXCHANGE_EXECUTION_CONTEXT, CHAIN_PRICES) >> Optional.empty()
        0 * _

        expect:
        chainExchangeService.tryToFormExpectedChainExchange(CHAIN_EXCHANGE_EXECUTION_CONTEXT, CHAIN_PRICES).isEmpty()
    }

    def 'should execute chain exchange successfully'() {
        when:
        def result = chainExchangeService.tryToExecute(EXPECTED_CHAIN_EXCHANGE, CHAIN_EXCHANGE_EXECUTION_CONTEXT)

        then:
        1 * orderExecutor.execute(CHAIN_ORDER_1, ORDER_EXECUTION_MODE) >> {
            return CHAIN_ORDER_1.toBuilder().status(FILLED).build()
        }
        1 * orderExecutor.execute(CHAIN_ORDER_2, ORDER_EXECUTION_MODE) >> {
            return CHAIN_ORDER_2.toBuilder().status(FILLED).build()
        }
        1 * orderExecutor.execute(CHAIN_ORDER_3, ORDER_EXECUTION_MODE) >> {
            return CHAIN_ORDER_3.toBuilder().status(FILLED).build()
        }
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
        _ * orderExecutor.execute(CHAIN_ORDER_1, ORDER_EXECUTION_MODE) >> {
            return CHAIN_ORDER_1.toBuilder().status(order1Status).build()
        }
        _ * orderExecutor.execute(CHAIN_ORDER_2, ORDER_EXECUTION_MODE) >> {
            return CHAIN_ORDER_2.toBuilder().status(order2Status).build()
        }
        _ * orderExecutor.execute(CHAIN_ORDER_3, ORDER_EXECUTION_MODE) >> {
            return CHAIN_ORDER_3.toBuilder().status(order3Status).build()
        }
        0 * _

        and:
        result.isEmpty()

        and:
        noExceptionThrown()

        where:
        orderNumber | order1Status | order2Status | order3Status
        1           | NOT_FILLED   | _            | _
        2           | FILLED       | NOT_FILLED   | _
        3           | FILLED       | FILLED       | NOT_FILLED
    }
}
