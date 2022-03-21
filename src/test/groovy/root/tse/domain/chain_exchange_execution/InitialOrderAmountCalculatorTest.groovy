package root.tse.domain.chain_exchange_execution

import spock.lang.Specification

import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.*

class InitialOrderAmountCalculatorTest extends Specification {

    private initialOrderAmountCalculator = new InitialOrderAmountCalculator()

    def 'should calculate initial order amount successfully'() {
        expect:
        initialOrderAmountCalculator.tryToCalculate(CHAIN_EXCHANGE_EXECUTION_CONTEXT, CHAIN_PRICES, ORDER_FEE_PERCENT).get() == 0.331d
    }

    def 'should return empty optional if amount for order1 is less than threshold'() {
        given:
        def context = CHAIN_EXCHANGE_EXECUTION_CONTEXT.toBuilder().amount(1d).build()

        expect:
        initialOrderAmountCalculator.tryToCalculate(context, CHAIN_PRICES, ORDER_FEE_PERCENT).isEmpty()
    }

    def 'should return empty optional if amount for order3 is less than threshold'() {
        given:
        def prices = [
            (CHAIN_SYMBOL_1) : [(BUY) : CHAIN_SYMBOL_1_BUY_PRICE],
            (CHAIN_SYMBOL_2) : [(SELL) : 0.000093d],
            (CHAIN_SYMBOL_3) : [(SELL) : CHAIN_SYMBOL_3_SELL_PRICE]
        ]

        expect:
        initialOrderAmountCalculator.tryToCalculate(CHAIN_EXCHANGE_EXECUTION_CONTEXT, prices, ORDER_FEE_PERCENT).isEmpty()
    }
}
