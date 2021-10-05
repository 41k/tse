package root.tse.domain.strategy_execution

import org.ta4j.core.Bar
import org.ta4j.core.num.PrecisionNum
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus
import root.tse.domain.strategy_execution.market_scanning.MarketScanningTask
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.trade.*
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledExecutorService

import static root.tse.util.TestData.*
import static root.tse.util.TestUtils.*
import static root.tse.domain.strategy_execution.trade.OrderStatus.*
import static root.tse.domain.strategy_execution.trade.OrderType.BUY
import static root.tse.domain.strategy_execution.trade.OrderType.SELL
import static root.tse.domain.strategy_execution.trade.TradeType.LONG

class StrategyExecutionTest extends Specification {

    private static final SYMBOLS = [SYMBOL_1] as Set

    private bar = Mock(Bar)
    private entryRule = Mock(EntryRule)
    private exitRule = Mock(ExitRule)
    private strategy = createStrategy(entryRule, exitRule)
    private marketScanningTaskExecutor = Mock(ScheduledExecutorService)
    private marketScanningTask = Mock(MarketScanningTask)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeExecution = Mock(TradeExecution)
    private orderExecutor = Mock(OrderExecutor)
    private tradeExecutionFactory = Mock(TradeExecutionFactory)
    private tradeRepository = Mock(TradeRepository)
    private eventBus = Mock(StrategyExecutionEventBus)
    private strategyExecutionContext = StrategyExecutionContext.builder()
        .strategy(strategy).symbols(SYMBOLS).executionMode(StrategyExecutionMode.TRADING)
        .allowedNumberOfSimultaneouslyOpenedTrades(NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES)
        .fundsPerTrade(FUNDS_PER_TRADE).build()

    private StrategyExecution strategyExecution

    def setup() {
        strategyExecution = new StrategyExecution(
            STRATEGY_EXECUTION_ID, strategyExecutionContext, marketScanningTaskExecutor,
            clockSignalDispatcher, orderExecutor, tradeExecutionFactory, tradeRepository, eventBus)
    }

    def 'should provide id'() {
        expect:
        strategyExecution.getId() == STRATEGY_EXECUTION_ID
    }

    def 'should be started correctly'() {
        when:
        strategyExecution.start()

        then:
        1 * entryRule.getHighestInterval() >> Interval.ONE_MINUTE
        1 * clockSignalDispatcher.subscribe(Interval.ONE_MINUTE, strategyExecution)
        0 * _
    }

    def 'should be stopped correctly'() {
        given: 'active market scanning task'
        strategyExecution.marketScanningTask = marketScanningTask

        and: 'active trade executions'
        def tradeExecution1 = Mock(TradeExecution)
        def tradeExecution2 = Mock(TradeExecution)
        strategyExecution.tradeExecutions << [
            (SYMBOL_1) : tradeExecution1,
            (SYMBOL_2) : tradeExecution2
        ]

        when:
        strategyExecution.stop()

        then:
        1 * entryRule.getHighestInterval() >> Interval.ONE_MINUTE
        1 * clockSignalDispatcher.unsubscribe(Interval.ONE_MINUTE, strategyExecution)
        1 * marketScanningTask.stop()
        1 * marketScanningTaskExecutor.shutdownNow()
        1 * tradeExecution1.stop()
        1 * tradeExecution2.stop()
        0 * _

        and:
        strategyExecution.tradeExecutions.isEmpty()
    }

    def 'should rerun market scanning task when clock signal is accepted'() {
        given: 'active market scanning task'
        strategyExecution.marketScanningTask = marketScanningTask

        and:
        def newMarketScanningTask = null

        when:
        strategyExecution.acceptClockSignal()

        then:
        1 * marketScanningTask.stop()
        1 * marketScanningTaskExecutor.submit(_) >> {
            newMarketScanningTask = it[0] as MarketScanningTask
            assert newMarketScanningTask != marketScanningTask
            assert newMarketScanningTask.entryRule == entryRule
            assert newMarketScanningTask.symbols == SYMBOLS
            assert newMarketScanningTask.strategyExecution == strategyExecution
            assert newMarketScanningTask.shouldScan.get()
        }
        0 * _

        and:
        strategyExecution.marketScanningTask == newMarketScanningTask
    }

    def 'should open trade successfully'() {
        given: 'strategy execution contains no trade executions'
        assert strategyExecution.tradeExecutions.isEmpty()

        when:
        strategyExecution.openTrade(SYMBOL_1, bar)

        then: 'execute entry order'
        1 * tradeRepository.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID) >> [CLOSED_TRADE, OPENED_TRADE]
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(PRICE_1)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_1), ZoneId.systemDefault())
        1 * orderExecutor.execute(_, StrategyExecutionMode.TRADING) >> {
            def entryOrder = it[0] as Order
            assertEntryOrderBeforeExecution(entryOrder)
            return entryOrder.toBuilder().status(FILLED).build()
        }

        and: 'create and save opened trade'
        1 * tradeRepository.save(_ as Trade) >> { assertOpenedTrade(it[0] as Trade) }

        and: 'start trade execution'
        1 * tradeExecutionFactory.create(_ as Trade, strategyExecution) >> {
            assertOpenedTrade(it[0] as Trade)
            return tradeExecution
        }
        1 * tradeExecution.start()
        strategyExecution.tradeExecutions.get(SYMBOL_1) == tradeExecution
        strategyExecution.tradeExecutions.size() == 1

        and: 'publish correct event'
        1 * eventBus.publishTradeWasOpenedEvent(_) >> { assertOpenedTrade(it[0] as Trade) }

        and: 'no other actions'
        0 * _
    }

    def 'should not open trade if trade execution exists for the same symbol'() {
        given: 'trade execution'
        strategyExecution.tradeExecutions << [(SYMBOL_1) : tradeExecution]

        when:
        strategyExecution.openTrade(SYMBOL_1, bar)

        then: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, 'there is trade execution for the same symbol')

        and: 'no other actions'
        0 * _

        and: 'no trade executions were started'
        strategyExecution.tradeExecutions.size() == 1
    }

    def 'should not open trade if allowed number of simultaneously opened trades has been reached'() {
        when:
        strategyExecution.openTrade(SYMBOL_1, bar)

        then: 'number of simultaneously opened trades has been reached'
        1 * tradeRepository.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID) >> [
            CLOSED_TRADE, OPENED_TRADE, CLOSED_TRADE, OPENED_TRADE, OPENED_TRADE, CLOSED_TRADE
        ]

        then: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, 'allowed number of simultaneously opened trades has been reached')

        and: 'no other actions'
        0 * _

        and: 'no trade executions were started'
        strategyExecution.tradeExecutions.isEmpty()
    }

    def 'should not open trade if entry order was not filled'() {
        when:
        strategyExecution.openTrade(SYMBOL_1, bar)

        then: 'failed entry order execution'
        1 * tradeRepository.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID) >> [CLOSED_TRADE, OPENED_TRADE]
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(PRICE_1)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(TIMESTAMP_1), ZoneId.systemDefault())
        1 * orderExecutor.execute(_, StrategyExecutionMode.TRADING) >> {
            def entryOrder = it[0] as Order
            assertEntryOrderBeforeExecution(entryOrder)
            return entryOrder.toBuilder().status(NOT_FILLED).build()
        }

        and: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, 'entry order was not filled')

        and: 'no other actions'
        0 * _

        and: 'no trade executions were started'
        strategyExecution.tradeExecutions.isEmpty()
    }

    def 'should close trade successfully'() {
        given: 'trade execution of the trade'
        strategyExecution.tradeExecutions << [(SYMBOL_1) : tradeExecution]

        when:
        strategyExecution.closeTrade(TRADE_TO_CLOSE)

        then: 'execute exit order'
        1 * orderExecutor.execute(_, StrategyExecutionMode.TRADING) >> {
            def exitOrder = it[0] as Order
            assertExitOrderBeforeExecution(exitOrder)
            return exitOrder.toBuilder().status(FILLED).build()
        }

        and: 'save closed trade'
        1 * tradeRepository.save(_) >> { assertClosedTrade(it[0]) }

        and: 'stop trade execution'
        1 * tradeExecution.stop()
        !strategyExecution.tradeExecutions.get(SYMBOL_1)

        and: 'publish correct event'
        1 * eventBus.publishTradeWasClosedEvent(_) >> { assertClosedTrade(it[0]) }

        and: 'no other actions'
        0 * _
    }

    def 'should not close trade if exit order was not filled'() {
        given: 'trade execution of the trade'
        strategyExecution.tradeExecutions << [(SYMBOL_1) : tradeExecution]

        when:
        strategyExecution.closeTrade(TRADE_TO_CLOSE)

        then: 'failed entry order execution'
        1 * orderExecutor.execute(_, StrategyExecutionMode.TRADING) >> {
            def exitOrder = it[0] as Order
            assertExitOrderBeforeExecution(exitOrder)
            return exitOrder.toBuilder().status(NOT_FILLED).build()
        }

        and: 'publish correct event'
        1 * eventBus.publishTradeWasNotClosedEvent(TRADE_TO_CLOSE, 'exit order was not filled')

        and: 'no other actions'
        0 * _

        and: 'trade execution was not stopped and still exist'
        strategyExecution.tradeExecutions.get(SYMBOL_1)
    }

    private assertEntryOrderBeforeExecution(Order entryOrder) {
        assert entryOrder.status == NEW
        assert entryOrder.type == BUY
        assert entryOrder.symbol == SYMBOL_1
        assert entryOrder.amount == AMOUNT_1
        assert entryOrder.price == PRICE_1
        assert entryOrder.timestamp == TIMESTAMP_1
    }

    private assertExitOrderBeforeExecution(Order exitOrder) {
        assert exitOrder.status == NEW
        assert exitOrder.type == SELL
        assert exitOrder.symbol == SYMBOL_1
        assert exitOrder.amount == AMOUNT_2
        assert exitOrder.price == PRICE_2
        assert exitOrder.timestamp == TIMESTAMP_2
    }

    private assertOpenedTrade(Trade openedTrade) {
        assert UUID.fromString(openedTrade.id)
        assert openedTrade.strategyExecutionId == STRATEGY_EXECUTION_ID
        assert openedTrade.type == LONG
        assert openedTrade.entryOrder.status == FILLED
        assert openedTrade.entryOrder.type == BUY
        assert openedTrade.entryOrder.symbol == SYMBOL_1
        assert openedTrade.entryOrder.amount == AMOUNT_1
        assert openedTrade.entryOrder.price == PRICE_1
        assert openedTrade.entryOrder.timestamp == TIMESTAMP_1
        assert !openedTrade.exitOrder
    }

    private assertClosedTrade(Trade closedTrade) {
        assert closedTrade.exitOrder.status == FILLED
        assert closedTrade.exitOrder.type == SELL
        assert closedTrade.exitOrder.symbol == SYMBOL_1
        assert closedTrade.exitOrder.amount == AMOUNT_2
        assert closedTrade.exitOrder.price == PRICE_2
        assert closedTrade.exitOrder.timestamp == TIMESTAMP_2
    }
}
