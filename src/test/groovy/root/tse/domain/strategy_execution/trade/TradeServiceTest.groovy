package root.tse.domain.strategy_execution.trade

import root.tse.domain.ExchangeGateway
import root.tse.domain.IdGenerator
import root.tse.domain.order.Order
import spock.lang.Specification

import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG
import static root.tse.util.TestUtils.*

class TradeServiceTest extends Specification {

    private idGenerator = Mock(IdGenerator)
    private exchangeGateway = Mock(ExchangeGateway)
    private tradeRepository = Mock(TradeRepository)

    private tradeService = new TradeService(idGenerator, exchangeGateway, tradeRepository)

    def 'should open trade successfully'() {
        when:
        def openedTradeOptional = tradeService.tryToOpenTrade(TRADE_OPENING_CONTEXT)

        then: 'execute entry order'
        1 * exchangeGateway.getCurrentPrices([SYMBOL_1]) >> Optional.of([(SYMBOL_1) : [(BUY) : PRICE_1]])
        1 * exchangeGateway.tryToExecute(_) >> {
            def entryOrder = it[0] as Order
            assertEntryOrderBeforeExecution(entryOrder)
            return Optional.of(entryOrder.toBuilder().price(PRICE_1).build())
        }

        and: 'create and save opened trade'
        1 * idGenerator.generateId() >> TRADE_ID
        1 * tradeRepository.save(_) >> { assertOpenedTrade(it[0] as Trade) }

        and: 'no other actions'
        0 * _

        and:
        def openedTrade = openedTradeOptional.get()
        assertOpenedTrade(openedTrade)
    }

    def 'should not open trade if current price was not obtained'() {
        when:
        def openedTradeOptional = tradeService.tryToOpenTrade(TRADE_OPENING_CONTEXT)

        then: 'failed to obtain current price'
        1 * exchangeGateway.getCurrentPrices([SYMBOL_1]) >> Optional.empty()
        0 * _

        and:
        openedTradeOptional.isEmpty()
    }

    def 'should not open trade if entry order was not executed'() {
        when:
        def openedTradeOptional = tradeService.tryToOpenTrade(TRADE_OPENING_CONTEXT)

        then: 'failed entry order execution'
        1 * exchangeGateway.getCurrentPrices([SYMBOL_1]) >> Optional.of([(SYMBOL_1) : [(BUY) : PRICE_1]])
        1 * exchangeGateway.tryToExecute(_) >> {
            def entryOrder = it[0] as Order
            assertEntryOrderBeforeExecution(entryOrder)
            return Optional.empty()
        }
        0 * _

        and:
        openedTradeOptional.isEmpty()
    }

    def 'should close trade successfully'() {
        when:
        def closedTradeOptional = tradeService.tryToCloseTrade(OPENED_TRADE, CLOCK_SIGNAL_2)

        then: 'execute exit order'
        1 * exchangeGateway.tryToExecute(_) >> {
            def exitOrder = it[0] as Order
            assertExitOrderBeforeExecution(exitOrder)
            return Optional.of(exitOrder.toBuilder().price(PRICE_2).build())
        }

        and: 'save closed trade'
        1 * tradeRepository.save(_) >> { assertClosedTrade(it[0] as Trade) }

        and: 'no other actions'
        0 * _

        and:
        def closedTrade = closedTradeOptional.get()
        assertClosedTrade(closedTrade)
    }

    def 'should not close trade if exit order was not executed'() {
        when:
        def closedTradeOptional = tradeService.tryToCloseTrade(OPENED_TRADE, CLOCK_SIGNAL_2)

        then: 'failed exit order execution'
        1 * exchangeGateway.tryToExecute(_) >> {
            def exitOrder = it[0] as Order
            assertExitOrderBeforeExecution(exitOrder)
            return Optional.empty()
        }
        0 * _

        and:
        closedTradeOptional.isEmpty()
    }

    def 'should get trades by strategy execution id'() {
        given:
        def trades = [OPENED_TRADE, CLOSED_TRADE]

        when:
        def result = tradeService.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID)

        then:
        1 * tradeRepository.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID) >> trades
        0 * _

        and:
        result == trades
    }

    private assertEntryOrderBeforeExecution(Order entryOrder) {
        assert entryOrder.type == BUY
        assert entryOrder.symbol == SYMBOL_1
        assert entryOrder.amount == AMOUNT_1
        assert !entryOrder.price
        assert entryOrder.timestamp == TIMESTAMP_1
    }

    private boolean assertOpenedTrade(Trade openedTrade) {
        assert openedTrade.id == TRADE_ID
        assert openedTrade.strategyExecutionId == STRATEGY_EXECUTION_ID
        assert openedTrade.type == LONG
        assert openedTrade.entryOrder.type == BUY
        assert openedTrade.entryOrder.symbol == SYMBOL_1
        assert openedTrade.entryOrder.amount == AMOUNT_1
        assert openedTrade.entryOrder.price == PRICE_1
        assert openedTrade.entryOrder.timestamp == TIMESTAMP_1
        assert !openedTrade.exitOrder
        true
    }

    private assertExitOrderBeforeExecution(Order exitOrder) {
        assert exitOrder.type == SELL
        assert exitOrder.symbol == SYMBOL_1
        assert exitOrder.amount == AMOUNT_1
        assert !exitOrder.price
        assert exitOrder.timestamp == TIMESTAMP_2
    }

    private boolean assertClosedTrade(Trade closedTrade) {
        assert closedTrade.id == TRADE_ID
        assert closedTrade.strategyExecutionId == STRATEGY_EXECUTION_ID
        assert closedTrade.type == LONG
        assert closedTrade.entryOrder == ENTRY_ORDER
        assert closedTrade.exitOrder.type == SELL
        assert closedTrade.exitOrder.symbol == SYMBOL_1
        assert closedTrade.exitOrder.amount == AMOUNT_1
        assert closedTrade.exitOrder.price == PRICE_2
        assert closedTrade.exitOrder.timestamp == TIMESTAMP_2
        true
    }
}
