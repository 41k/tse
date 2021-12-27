package root.tse.domain.chain_exchange_execution

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import root.tse.BaseFunctionalTest
import root.tse.domain.order.OrderExecutionMode

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.*

@ContextConfiguration(classes = [TestContextConfiguration])
class ChainExchangeExecutionFunctionalTest extends BaseFunctionalTest {

    private static final ORDER_EXECUTION_REQUEST = [
        (CHAIN_SYMBOL_1) : ORDER_EXECUTION_REQUEST_TEMPLATE
            .replace('$AMOUNT', CHAIN_ORDER_1_AMOUNT as String)
            .replace('$ORDER_TYPE', 'BUY')
            .replace('$SYMBOL', applyUrlEncoding(CHAIN_SYMBOL_1))
            .replace('$TIMESTAMP', CHAIN_EXCHANGE_EXECUTION_TIMESTAMP as String)
            .replace('$SIGNATURE', 'a6f44c6fff0689bb18697de688f900855f00d56bb3382afbe3eb9fbbca454ab9'),
        (CHAIN_SYMBOL_2) : ORDER_EXECUTION_REQUEST_TEMPLATE
            .replace('$AMOUNT', CHAIN_ORDER_2_AMOUNT as String)
            .replace('$ORDER_TYPE', 'SELL')
            .replace('$SYMBOL', applyUrlEncoding(CHAIN_SYMBOL_2))
            .replace('$TIMESTAMP', CHAIN_EXCHANGE_EXECUTION_TIMESTAMP as String)
            .replace('$SIGNATURE', '1238dcbb203e351b68aa0ab7378b94daf47fa180f9128a0cc9ae4ba58a28ea0f'),
        (CHAIN_SYMBOL_3) : ORDER_EXECUTION_REQUEST_TEMPLATE
            .replace('$AMOUNT', CHAIN_ORDER_3_AMOUNT as String)
            .replace('$ORDER_TYPE', 'SELL')
            .replace('$SYMBOL', applyUrlEncoding(CHAIN_SYMBOL_3))
            .replace('$TIMESTAMP', CHAIN_EXCHANGE_EXECUTION_TIMESTAMP as String)
            .replace('$SIGNATURE', '7705db3d73d7ed450c35896046dae0ea332612dd8ebe32ce18f561d88a7ff671')
    ]
    private static final ORDER_EXECUTION_RESPONSE = [
        (CHAIN_SYMBOL_1) : ORDER_EXECUTION_RESPONSE_TEMPLATE
            .replace('$AMOUNT', CHAIN_ORDER_1_AMOUNT as String)
            .replace('$PRICE', CHAIN_SYMBOL_1_BUY_PRICE as String),
        (CHAIN_SYMBOL_2) : ORDER_EXECUTION_RESPONSE_TEMPLATE
            .replace('$AMOUNT', CHAIN_ORDER_2_AMOUNT as String)
            .replace('$PRICE', CHAIN_SYMBOL_2_SELL_PRICE as String),
        (CHAIN_SYMBOL_3) : ORDER_EXECUTION_RESPONSE_TEMPLATE
            .replace('$AMOUNT', CHAIN_ORDER_3_AMOUNT as String)
            .replace('$PRICE', CHAIN_SYMBOL_3_SELL_PRICE as String)
    ]

    @Autowired
    private ChainExchangeExecutionFactory chainExchangeExecutionFactory

    private ChainExchangeExecution chainExchangeExecution

    def setup() {
        def context = ChainExchangeExecutionContext.builder()
            .assetChain(ASSET_CHAIN)
            .assetCodeDelimiter(ASSET_CODE_DELIMITER)
            .symbolToPrecisionMap(SYMBOL_TO_PRECISION_MAP)
            .orderFeePercent(ORDER_FEE_PERCENT)
            .nAmountSelectionSteps(N_AMOUNT_SELECTION_STEPS)
            .amount(CHAIN_EXCHANGE_AMOUNT)
            .minProfitThreshold(MIN_PROFIT_THRESHOLD)
            .orderExecutionMode(OrderExecutionMode.EXCHANGE_GATEWAY)
            .build()
        chainExchangeExecution = chainExchangeExecutionFactory.create(context)
    }

    def 'should execute chain exchange successfully'() {
        given: 'no chain exchanges for now'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()

        and:
        setCurrentPrices(CHAIN_PRICES)

        and:
        mockSuccessfulOrderExecution(CHAIN_SYMBOL_1)
        mockSuccessfulOrderExecution(CHAIN_SYMBOL_2)
        mockSuccessfulOrderExecution(CHAIN_SYMBOL_3)

        when:
        chainExchangeExecution.run()

        then:
        def chainExchanges = chainExchangeDbEntryJpaRepository.findAll()
        chainExchanges.size() == 1

        and:
        chainExchanges.get(0).id
        chainExchanges.get(0).assetChain == ASSET_CHAIN_AS_STRING
        chainExchanges.get(0).orderFeePercent == ORDER_FEE_PERCENT
        chainExchanges.get(0).executionTimestamp.toEpochMilli() == CHAIN_EXCHANGE_EXECUTION_TIMESTAMP
        chainExchanges.get(0).order1Type == BUY
        chainExchanges.get(0).order1Symbol == CHAIN_SYMBOL_1
        chainExchanges.get(0).order1Amount == CHAIN_ORDER_1_AMOUNT
        chainExchanges.get(0).order1Price == CHAIN_SYMBOL_1_BUY_PRICE
        chainExchanges.get(0).order2Type == SELL
        chainExchanges.get(0).order2Symbol == CHAIN_SYMBOL_2
        chainExchanges.get(0).order2Amount == CHAIN_ORDER_2_AMOUNT
        chainExchanges.get(0).order2Price == CHAIN_SYMBOL_2_SELL_PRICE
        chainExchanges.get(0).order3Type == SELL
        chainExchanges.get(0).order3Symbol == CHAIN_SYMBOL_3
        chainExchanges.get(0).order3Amount == CHAIN_ORDER_3_AMOUNT
        chainExchanges.get(0).order3Price == CHAIN_SYMBOL_3_SELL_PRICE
        chainExchanges.get(0).profit == CHAIN_EXCHANGE_PROFIT
    }

    def 'should not execute chain exchange if required current prices are not provided by exchange gateway'() {
        given: 'no chain exchanges for now'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()

        and: 'no current prices'
        setCurrentPrices([:])

        when:
        chainExchangeExecution.run()

        then: 'no chain exchanges were executed'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not execute chain exchange if expected profit is lower than threshold'() {
        given: 'no chain exchanges for now'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()

        and: 'sell price for symbol3 which yields profit lower than threshold'
        setCurrentPrices([
            (CHAIN_SYMBOL_1) : [(BUY) : CHAIN_SYMBOL_1_BUY_PRICE, (SELL) : CHAIN_SYMBOL_1_SELL_PRICE],
            (CHAIN_SYMBOL_2) : [(BUY) : CHAIN_SYMBOL_2_BUY_PRICE, (SELL) : CHAIN_SYMBOL_2_SELL_PRICE],
            (CHAIN_SYMBOL_3) : [(BUY) : CHAIN_SYMBOL_3_BUY_PRICE, (SELL) : 42300d]
        ])

        when:
        chainExchangeExecution.run()

        then: 'no chain exchanges were executed'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not complete chain exchange if one of chain orders failed'() {
        given: 'no chain exchanges for now'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()

        and:
        setCurrentPrices(CHAIN_PRICES)

        and:
        mockSuccessfulOrderExecution(CHAIN_SYMBOL_1)
        mockFailedOrderExecution(CHAIN_SYMBOL_2)

        when:
        chainExchangeExecution.run()

        then: 'no chain exchanges were completed'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()
    }

    private void mockSuccessfulOrderExecution(String symbol) {
        def requestBody = ORDER_EXECUTION_REQUEST.get(symbol)
        def responseBody = ORDER_EXECUTION_RESPONSE.get(symbol)
        mockSuccessfulOrderExecutionCall(requestBody, responseBody)
    }

    private void mockFailedOrderExecution(String symbol) {
        def requestBody = ORDER_EXECUTION_REQUEST.get(symbol)
        mockFailedOrderExecutionCall(requestBody)
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        Clock clock() {
            Clock.fixed(Instant.ofEpochMilli(CHAIN_EXCHANGE_EXECUTION_TIMESTAMP), ZoneId.systemDefault())
        }
    }
}
