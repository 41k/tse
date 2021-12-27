package root.tse.domain.strategy_execution.trade

import org.ta4j.core.Bar
import org.ta4j.core.num.PrecisionNum
import root.tse.domain.IdGenerator
import root.tse.domain.order.Order
import root.tse.domain.order.OrderExecutor
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime

import static root.tse.domain.order.OrderStatus.*
import static root.tse.domain.order.OrderType.BUY
import static root.tse.domain.order.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG
import static root.tse.util.TestUtils.*

class TradeServiceTest extends Specification {

    private bar = Mock(Bar)
    private tradeOpeningContext = createTradeOpeningContext(bar)
    private tradeClosingContext = createTradeClosingContext(bar)
    private idGenerator = Mock(IdGenerator)
    private orderExecutor = Mock(OrderExecutor)
    private tradeRepository = Mock(TradeRepository)

    private tradeService = new TradeService(idGenerator, orderExecutor, tradeRepository)

    def 'should open trade successfully'() {
        when:
        def openedTradeOptional = tradeService.tryToOpenTrade(tradeOpeningContext)

        then: 'execute entry order'
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(PRICE_1)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_1), ZoneId.systemDefault())
        1 * orderExecutor.execute(_, ORDER_EXECUTION_MODE) >> {
            def entryOrder = it[0] as Order
            assertEntryOrderBeforeExecution(entryOrder)
            return entryOrder.toBuilder().status(FILLED).build()
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

    def 'should not open trade if entry order was not filled'() {
        when:
        def openedTradeOptional = tradeService.tryToOpenTrade(tradeOpeningContext)

        then: 'failed entry order execution'
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(PRICE_1)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_1), ZoneId.systemDefault())
        1 * orderExecutor.execute(_, ORDER_EXECUTION_MODE) >> {
            def entryOrder = it[0] as Order
            assertEntryOrderBeforeExecution(entryOrder)
            return entryOrder.toBuilder().status(NOT_FILLED).build()
        }

        and: 'no other actions'
        0 * _

        and:
        openedTradeOptional.isEmpty()
    }

    def 'should close trade successfully'() {
        when:
        def closedTradeOptional = tradeService.tryToCloseTrade(tradeClosingContext)

        then: 'execute exit order'
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(PRICE_2)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_2), ZoneId.systemDefault())
        1 * orderExecutor.execute(_, ORDER_EXECUTION_MODE) >> {
            def exitOrder = it[0] as Order
            assertExitOrderBeforeExecution(exitOrder)
            return exitOrder.toBuilder().status(FILLED).build()
        }

        and: 'save closed trade'
        1 * tradeRepository.save(_) >> { assertClosedTrade(it[0] as Trade) }

        and: 'no other actions'
        0 * _

        and:
        def closedTrade = closedTradeOptional.get()
        assertClosedTrade(closedTrade)
    }

    def 'should not close trade if exit order was not filled'() {
        when:
        def closedTradeOptional = tradeService.tryToCloseTrade(tradeClosingContext)

        then: 'failed exit order execution'
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(PRICE_2)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_2), ZoneId.systemDefault())
        1 * orderExecutor.execute(_, ORDER_EXECUTION_MODE) >> {
            def exitOrder = it[0] as Order
            assertExitOrderBeforeExecution(exitOrder)
            return exitOrder.toBuilder().status(NOT_FILLED).build()
        }

        and: 'no other actions'
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
        assert entryOrder.status == NEW
        assert entryOrder.type == BUY
        assert entryOrder.symbol == SYMBOL_1
        assert entryOrder.amount == AMOUNT_1
        assert entryOrder.price == PRICE_1
        assert entryOrder.timestamp == TIMESTAMP_1
    }

    private boolean assertOpenedTrade(Trade openedTrade) {
        assert openedTrade.id == TRADE_ID
        assert openedTrade.strategyExecutionId == STRATEGY_EXECUTION_ID
        assert openedTrade.type == LONG
        assert openedTrade.entryOrder.status == FILLED
        assert openedTrade.entryOrder.type == BUY
        assert openedTrade.entryOrder.symbol == SYMBOL_1
        assert openedTrade.entryOrder.amount == AMOUNT_1
        assert openedTrade.entryOrder.price == PRICE_1
        assert openedTrade.entryOrder.timestamp == TIMESTAMP_1
        assert !openedTrade.exitOrder
        true
    }

    private assertExitOrderBeforeExecution(Order exitOrder) {
        assert exitOrder.status == NEW
        assert exitOrder.type == SELL
        assert exitOrder.symbol == SYMBOL_1
        assert exitOrder.amount == AMOUNT_1
        assert exitOrder.price == PRICE_2
        assert exitOrder.timestamp == TIMESTAMP_2
    }

    private boolean assertClosedTrade(Trade closedTrade) {
        assert closedTrade.id == TRADE_ID
        assert closedTrade.strategyExecutionId == STRATEGY_EXECUTION_ID
        assert closedTrade.type == LONG
        assert closedTrade.entryOrder == ENTRY_ORDER
        assert closedTrade.exitOrder.status == FILLED
        assert closedTrade.exitOrder.type == SELL
        assert closedTrade.exitOrder.symbol == SYMBOL_1
        assert closedTrade.exitOrder.amount == AMOUNT_1
        assert closedTrade.exitOrder.price == PRICE_2
        assert closedTrade.exitOrder.timestamp == TIMESTAMP_2
        true
    }
}
