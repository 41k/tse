package root.tse.domain.strategy_execution

import org.ta4j.core.Bar
import org.ta4j.core.num.PrecisionNum
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus
import root.tse.domain.strategy_execution.funds.FundsManager
import root.tse.domain.strategy_execution.market_scanning.MarketScanningTask
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.trade.*
import spock.lang.Specification

import java.time.Instant
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.ScheduledExecutorService

import static root.tse.domain.strategy_execution.trade.OrderStatus.*
import static root.tse.domain.strategy_execution.trade.OrderType.BUY
import static root.tse.domain.strategy_execution.trade.OrderType.SELL

class StrategyExecutionTest extends Specification {

    private static final STRATEGY_EXECUTION_ID = 'STRATEGY-EXECUTION-1'
    private static final STRATEGY_EXECUTION_TYPE = StrategyExecutionType.TRADING
    private static final TRADE_TYPE = TradeType.LONG
    private static final SYMBOL = 'symbol'
    private static final SYMBOLS = [SYMBOL] as Set
    private static final INTERVAL = Interval.ONE_MINUTE
    private static final AMOUNT = 2.5d
    private static final LAST_BAR_CLOSE_PRICE = 2000d
    private static final PRICE_AT_ORDER_EXECUTION_TIME = 2010d
    private static final LAST_BAR_TIMESTAMP = 1631106725000L

    private bar = Mock(Bar)
    private entryRule = Mock(EntryRule)
    private exitRule = Mock(ExitRule)
    private strategy = createStrategy(entryRule, exitRule)
    private marketScanningTaskExecutor = Mock(ScheduledExecutorService)
    private marketScanningTask = Mock(MarketScanningTask)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeExecution = Mock(TradeExecution)
    private fundsManager = Mock(FundsManager)
    private orderExecutor = Mock(OrderExecutor)
    private tradeExecutionFactory = Mock(TradeExecutionFactory)
    private tradeRepository = Mock(TradeRepository)
    private eventBus = Mock(StrategyExecutionEventBus)

    private StrategyExecution strategyExecution

    def setup() {
        strategyExecution = new StrategyExecution(
            STRATEGY_EXECUTION_ID, strategy, SYMBOLS, STRATEGY_EXECUTION_TYPE, marketScanningTaskExecutor,
            clockSignalDispatcher, fundsManager, orderExecutor, tradeExecutionFactory, tradeRepository, eventBus)
    }

    def 'should provide id'() {
        expect:
        strategyExecution.getId() == STRATEGY_EXECUTION_ID
    }

    def 'should be started correctly'() {
        when:
        strategyExecution.start()

        then:
        1 * entryRule.getHighestInterval() >> INTERVAL
        1 * clockSignalDispatcher.subscribe(INTERVAL, strategyExecution)
        0 * _
    }

    def 'should be stopped correctly'() {
        given: 'active market scanning task'
        strategyExecution.marketScanningTask = marketScanningTask

        and: 'active trade executions'
        def tradeExecution1 = Mock(TradeExecution)
        def tradeExecution2 = Mock(TradeExecution)
        strategyExecution.tradeExecutions << [
            'symbol-1' : tradeExecution1,
            'symbol-2' : tradeExecution2
        ]

        when:
        strategyExecution.stop()

        then:
        1 * entryRule.getHighestInterval() >> INTERVAL
        1 * clockSignalDispatcher.unsubscribe(INTERVAL, strategyExecution)
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
        strategyExecution.openTrade(SYMBOL, bar)

        then: 'execute entry order'
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(LAST_BAR_CLOSE_PRICE)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(LAST_BAR_TIMESTAMP), ZoneId.systemDefault())
        1 * fundsManager.acquireFundsAndProvideTradeAmount(LAST_BAR_CLOSE_PRICE) >> AMOUNT
        1 * orderExecutor.execute(_, STRATEGY_EXECUTION_TYPE) >> {
            def entryOrder = it[0] as Order
            assertOrderBeforeExecution(entryOrder, BUY)
            return entryOrder.toBuilder()
                .status(FILLED)
                // usually order is executed at a little bit different price due to time lag
                .price(PRICE_AT_ORDER_EXECUTION_TIME)
                .build()
        }

        and: 'create and save opened trade'
        1 * tradeRepository.save(_ as Trade) >> { assertOpenedTrade(it[0] as Trade) }

        and: 'start trade execution'
        1 * tradeExecutionFactory.create(_ as Trade, strategyExecution) >> {
            assertOpenedTrade(it[0] as Trade)
            return tradeExecution
        }
        1 * tradeExecution.start()
        strategyExecution.tradeExecutions.get(SYMBOL) == tradeExecution
        strategyExecution.tradeExecutions.size() == 1

        and: 'publish correct event'
        1 * eventBus.publishTradeWasOpenedEvent(_) >> { assertOpenedTrade(it[0] as Trade) }

        and: 'no other actions'
        0 * _
    }

    def 'should not open trade if trade execution exists for the same symbol'() {
        given: 'trade execution'
        strategyExecution.tradeExecutions << [(SYMBOL) : tradeExecution]

        when:
        strategyExecution.openTrade(SYMBOL, bar)

        then: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL, 'there is trade execution for the same symbol')

        and: 'no other actions'
        0 * _

        and: 'no trade executions were started'
        strategyExecution.tradeExecutions.size() == 1
    }

    def 'should not open trade if funds are not enough for entry order'() {
        when:
        strategyExecution.openTrade(SYMBOL, bar)

        then: 'funds are not enough'
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(LAST_BAR_CLOSE_PRICE)
        1 * fundsManager.acquireFundsAndProvideTradeAmount(LAST_BAR_CLOSE_PRICE) >> null

        then: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL, 'not enough funds for entry order')

        and: 'no other actions'
        0 * _

        and: 'no trade executions were started'
        strategyExecution.tradeExecutions.isEmpty()
    }

    def 'should not open trade if entry order was not filled'() {
        when:
        strategyExecution.openTrade(SYMBOL, bar)

        then: 'failed entry order execution'
        1 * bar.getClosePrice() >> PrecisionNum.valueOf(LAST_BAR_CLOSE_PRICE)
        1 * bar.getEndTime() >> ZonedDateTime.ofInstant(Instant.ofEpochMilli(LAST_BAR_TIMESTAMP), ZoneId.systemDefault())
        1 * fundsManager.acquireFundsAndProvideTradeAmount(LAST_BAR_CLOSE_PRICE) >> AMOUNT
        1 * orderExecutor.execute(_, STRATEGY_EXECUTION_TYPE) >> {
            def entryOrder = it[0] as Order
            assertOrderBeforeExecution(entryOrder, BUY)
            return entryOrder.toBuilder().status(NOT_FILLED).build()
        }

        and: 'return funds which were acquired for the trade'
        1 * fundsManager.returnFunds()

        and: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL, 'entry order was not filled')

        and: 'no other actions'
        0 * _

        and: 'no trade executions were started'
        strategyExecution.tradeExecutions.isEmpty()
    }

    def 'should close trade successfully'() {
        given: 'trade to close'
        def tradeToClose = createTradeToClose()

        and: 'trade execution of the trade'
        strategyExecution.tradeExecutions << [(SYMBOL) : tradeExecution]

        when:
        strategyExecution.closeTrade(tradeToClose)

        then: 'execute exit order'
        1 * orderExecutor.execute(_, STRATEGY_EXECUTION_TYPE) >> {
            def exitOrderBeforeExecution = it[0] as Order
            assertOrderBeforeExecution(exitOrderBeforeExecution, SELL)
            return exitOrderBeforeExecution.toBuilder()
                .status(FILLED)
                // usually order is executed at a little bit different price due to time lag
                .price(PRICE_AT_ORDER_EXECUTION_TIME)
                .build()
        }

        and: 'save closed trade'
        1 * tradeRepository.save(_) >> { assertClosedTrade(it[0]) }

        and: 'return funds which were acquired for the trade'
        1 * fundsManager.returnFunds()

        and: 'stop trade execution'
        1 * tradeExecution.stop()
        !strategyExecution.tradeExecutions.get(SYMBOL)

        and: 'publish correct event'
        1 * eventBus.publishTradeWasClosedEvent(_) >> { assertClosedTrade(it[0]) }

        and: 'no other actions'
        0 * _
    }

    def 'should not close trade if exit order was not filled'() {
        given: 'trade to close'
        def tradeToClose = createTradeToClose()

        and: 'trade execution of the trade'
        strategyExecution.tradeExecutions << [(SYMBOL) : tradeExecution]

        when:
        strategyExecution.closeTrade(tradeToClose)

        then: 'failed entry order execution'
        1 * orderExecutor.execute(_, STRATEGY_EXECUTION_TYPE) >> {
            def exitOrderBeforeExecution = it[0] as Order
            assertOrderBeforeExecution(exitOrderBeforeExecution, SELL)
            return exitOrderBeforeExecution.toBuilder().status(NOT_FILLED).build()
        }

        and: 'publish correct event'
        1 * eventBus.publishTradeWasNotClosedEvent(tradeToClose, 'exit order was not filled')

        and: 'no other actions'
        0 * _

        and: 'trade execution was not stopped and still exist'
        strategyExecution.tradeExecutions.get(SYMBOL)
    }

    private assertOrderBeforeExecution(Order order, OrderType orderType) {
        assert order.status == NEW
        assert order.type == orderType
        assert order.symbol == SYMBOL
        assert order.amount == AMOUNT
        assert order.price == LAST_BAR_CLOSE_PRICE
        assert order.timestamp == LAST_BAR_TIMESTAMP
    }

    private assertOpenedTrade(Trade openedTrade) {
        assert UUID.fromString(openedTrade.id)
        assert openedTrade.strategyExecutionId == STRATEGY_EXECUTION_ID
        assert openedTrade.type == TRADE_TYPE
        assert openedTrade.entryOrder.status == FILLED
        assert openedTrade.entryOrder.type == BUY
        assert openedTrade.entryOrder.symbol == SYMBOL
        assert openedTrade.entryOrder.amount == AMOUNT
        assert openedTrade.entryOrder.price == PRICE_AT_ORDER_EXECUTION_TIME
        assert openedTrade.entryOrder.timestamp == LAST_BAR_TIMESTAMP
        assert !openedTrade.exitOrder
    }

    private assertClosedTrade(Trade closedTrade) {
        assert closedTrade.exitOrder.status == FILLED
        assert closedTrade.exitOrder.type == SELL
        assert closedTrade.exitOrder.symbol == SYMBOL
        assert closedTrade.exitOrder.amount == AMOUNT
        assert closedTrade.exitOrder.price == PRICE_AT_ORDER_EXECUTION_TIME
        assert closedTrade.exitOrder.timestamp == LAST_BAR_TIMESTAMP
    }

    private Trade createTradeToClose() {
        def entryOrder = Order.builder().symbol(SYMBOL).build()
        def exitOrder = Order.builder()
            .type(SELL)
            .symbol(SYMBOL)
            .amount(AMOUNT)
            .price(LAST_BAR_CLOSE_PRICE)
            .timestamp(LAST_BAR_TIMESTAMP)
            .build()
        Trade.builder()
            .entryOrder(entryOrder)
            .exitOrder(exitOrder)
            .build()
    }

    private Strategy createStrategy(EntryRule entryRule, ExitRule exitRule) {
        new Strategy() {
            @Override
            String getId() {
                return '1'
            }
            @Override
            String getName() {
                return 'strategy-1'
            }
            @Override
            TradeType getTradeType() {
                return TRADE_TYPE
            }
            @Override
            EntryRule getEntryRule() {
                return entryRule
            }
            @Override
            ExitRule getExitRule() {
                return exitRule
            }
        }
    }
}
