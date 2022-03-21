package root.tse.domain.chain_exchange_execution

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ContextConfiguration
import root.tse.BaseFunctionalTest

import java.time.Clock
import java.time.Instant
import java.time.ZoneId

import static root.tse.domain.order.OrderExecutionType.MARKET
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.util.TestUtils.*

@ContextConfiguration(classes = [TestContextConfiguration])
class ChainExchangeExecutionFunctionalTest extends BaseFunctionalTest {

    @Autowired
    private ChainExchangeExecutionFactory chainExchangeExecutionFactory

    private ChainExchangeExecution chainExchangeExecution

    def setup() {
        exchangeGatewayMock().reset()
        exchangeGatewayMock().orderFeePercent = ORDER_FEE_PERCENT
        exchangeGatewayMock().currentPrices = CHAIN_PRICES
        exchangeGatewayMock().orderExecutionSuccess = true
        def context = ChainExchangeExecutionContext.builder()
            .assetChain(ASSET_CHAIN)
            .assetCodeDelimiter(ASSET_CODE_DELIMITER)
            .symbolToPrecisionMap(SYMBOL_TO_PRECISION_MAP)
            .nAmountSelectionSteps(N_AMOUNT_SELECTION_STEPS)
            .amount(CHAIN_EXCHANGE_AMOUNT)
            .minProfitThreshold(MIN_PROFIT_THRESHOLD)
            .orderExecutionType(MARKET)
            .build()
        chainExchangeExecution = chainExchangeExecutionFactory.create(context)
    }

    def 'should execute chain exchange successfully'() {
        given: 'no chain exchanges for now'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()

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
        chainExchanges.get(0).orderExecutionType == MARKET
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
        exchangeGatewayMock().currentPrices = null

        when:
        chainExchangeExecution.run()

        then: 'no chain exchanges were executed'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not execute chain exchange if expected profit is lower than threshold'() {
        given: 'no chain exchanges for now'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()

        and: 'sell price for symbol3 which yields profit lower than threshold'
        exchangeGatewayMock().currentPrices = [
            (CHAIN_SYMBOL_1) : [(BUY) : CHAIN_SYMBOL_1_BUY_PRICE, (SELL) : CHAIN_SYMBOL_1_SELL_PRICE],
            (CHAIN_SYMBOL_2) : [(BUY) : CHAIN_SYMBOL_2_BUY_PRICE, (SELL) : CHAIN_SYMBOL_2_SELL_PRICE],
            (CHAIN_SYMBOL_3) : [(BUY) : CHAIN_SYMBOL_3_BUY_PRICE, (SELL) : 42300d]
        ]

        when:
        chainExchangeExecution.run()

        then: 'no chain exchanges were executed'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()
    }

    def 'should not complete chain exchange if one of chain orders failed'() {
        given: 'no chain exchanges for now'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()

        and:
        exchangeGatewayMock().orderExecutionSuccess = false

        when:
        chainExchangeExecution.run()

        then: 'no chain exchanges were completed'
        assert chainExchangeDbEntryJpaRepository.findAll().isEmpty()
    }

    @TestConfiguration
    static class TestContextConfiguration {
        @Bean
        Clock clock() { Clock.fixed(Instant.ofEpochMilli(CHAIN_EXCHANGE_EXECUTION_TIMESTAMP), ZoneId.systemDefault()) }
    }
}
