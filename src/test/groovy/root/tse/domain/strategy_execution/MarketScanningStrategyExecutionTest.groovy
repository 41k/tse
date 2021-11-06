package root.tse.domain.strategy_execution

import org.ta4j.core.Bar
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus
import root.tse.domain.strategy_execution.market_scanning.MarketScanningTask
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.trade.TradeExecution
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory
import root.tse.domain.strategy_execution.trade.TradeService
import spock.lang.Specification

import java.util.concurrent.ScheduledExecutorService

import static org.apache.commons.lang3.StringUtils.EMPTY
import static root.tse.util.TestUtils.*

class MarketScanningStrategyExecutionTest extends Specification {

    private static final SYMBOLS = [SYMBOL_1]
    private static final ENTRY_RULE_HIGHEST_INTERVAL = Interval.TWELVE_HOURS

    private bar = Mock(Bar)
    private entryRule = Mock(EntryRule)
    private exitRule = Mock(ExitRule)
    private strategy = createStrategy(entryRule, exitRule)
    private strategyExecutionContext = StrategyExecutionContext.builder()
        .strategy(strategy).symbols(SYMBOLS).strategyExecutionMode(STRATEGY_EXECUTION_MODE)
        .fundsPerTrade(FUNDS_PER_TRADE).transactionFeePercent(TRANSACTION_FEE_PERCENT)
        .allowedNumberOfSimultaneouslyOpenedTrades(NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES).build()
    private marketScanningTaskExecutor = Mock(ScheduledExecutorService)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeService = Mock(TradeService)
    private tradeExecutionFactory = Mock(TradeExecutionFactory)
    private eventBus = Mock(StrategyExecutionEventBus)
    private marketScanningTask = Mock(MarketScanningTask)
    private tradeExecution = Mock(TradeExecution)
    private tradeOpeningContext = createTradeOpeningContext(bar)
    private tradeClosingContext = createTradeClosingContext(bar)

    private MarketScanningStrategyExecution strategyExecution

    def setup() {
        strategyExecution = new MarketScanningStrategyExecution(
            STRATEGY_EXECUTION_ID, strategyExecutionContext, marketScanningTaskExecutor,
            clockSignalDispatcher, tradeService, tradeExecutionFactory, eventBus, CLOCK)
    }

    def 'should provide id'() {
        expect:
        strategyExecution.getId() == STRATEGY_EXECUTION_ID
    }

    def 'should be started correctly'() {
        when:
        strategyExecution.start()

        then:
        1 * entryRule.getHighestInterval() >> ENTRY_RULE_HIGHEST_INTERVAL
        1 * clockSignalDispatcher.subscribe([ENTRY_RULE_HIGHEST_INTERVAL] as Set, strategyExecution)
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
        1 * entryRule.getHighestInterval() >> ENTRY_RULE_HIGHEST_INTERVAL
        1 * clockSignalDispatcher.unsubscribe([ENTRY_RULE_HIGHEST_INTERVAL] as Set, strategyExecution)
        1 * marketScanningTask.stop()
        1 * tradeExecution1.stop()
        1 * tradeExecution2.stop()
        0 * _

        and:
        strategyExecution.tradeExecutions.isEmpty()
    }

    def 'should rerun market scanning task when valid clock signal is provided'() {
        given: 'active market scanning task'
        strategyExecution.marketScanningTask = marketScanningTask

        and:
        def clockSignal = createClockSignal(Interval.FOUR_HOURS)

        and:
        def newMarketScanningTask = null

        when:
        strategyExecution.accept(clockSignal)

        then:
        1 * entryRule.getHighestInterval() >> Interval.FOUR_HOURS
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

    def 'should not rerun market scanning task when not valid clock signal is provided'() {
        given: 'active market scanning task'
        strategyExecution.marketScanningTask = marketScanningTask

        and:
        def clockSignal = createClockSignal(Interval.ONE_HOUR)

        when:
        strategyExecution.accept(clockSignal)

        then:
        1 * entryRule.getHighestInterval() >> Interval.FOUR_HOURS
        0 * _

        and: 'market scanning task is still the same'
        strategyExecution.marketScanningTask == marketScanningTask
    }

    def 'should start trade execution successfully'() {
        given: 'no trade executions have been started yet'
        assert strategyExecution.tradeExecutions.isEmpty()

        when:
        strategyExecution.openTrade(SYMBOL_1, bar)

        then: 'try to open trade'
        1 * tradeService.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID) >> [CLOSED_TRADE, OPENED_TRADE]
        1 * tradeService.tryToOpenTrade(tradeOpeningContext) >> Optional.of(OPENED_TRADE)

        and: 'start trade execution'
        1 * tradeExecutionFactory.create(OPENED_TRADE, strategyExecution) >> tradeExecution
        1 * tradeExecution.start()
        strategyExecution.tradeExecutions.get(SYMBOL_1) == tradeExecution
        strategyExecution.tradeExecutions.size() == 1

        and: 'publish correct event'
        1 * eventBus.publishTradeWasOpenedEvent(OPENED_TRADE)

        and: 'no other actions'
        0 * _
    }

    def 'should not start trade execution if another trade execution exists for the same symbol'() {
        given: 'existing trade execution'
        strategyExecution.tradeExecutions << [(SYMBOL_1) : tradeExecution]

        when:
        strategyExecution.openTrade(SYMBOL_1, bar)

        then: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, '(there is a trade execution for the same symbol)')

        and: 'no other actions'
        0 * _

        and: 'no trade executions were started'
        strategyExecution.tradeExecutions.size() == 1
    }

    def 'should not start trade execution if allowed number of simultaneously opened trades has been reached'() {
        given: 'no trade executions have been started yet'
        assert strategyExecution.tradeExecutions.isEmpty()

        when:
        strategyExecution.openTrade(SYMBOL_1, bar)

        then: 'number of simultaneously opened trades has been reached'
        1 * tradeService.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID) >> [
            CLOSED_TRADE, OPENED_TRADE, CLOSED_TRADE, OPENED_TRADE, OPENED_TRADE, CLOSED_TRADE
        ]

        then: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, '(allowed number of simultaneously opened trades has been reached)')

        and: 'no other actions'
        0 * _

        and: 'no trade executions were started'
        strategyExecution.tradeExecutions.isEmpty()
    }

    def 'should not start trade execution if trade was not opened'() {
        given: 'no trade executions have been started yet'
        assert strategyExecution.tradeExecutions.isEmpty()

        when:
        strategyExecution.openTrade(SYMBOL_1, bar)

        then: 'try to open trade'
        1 * tradeService.getAllTradesByStrategyExecutionId(STRATEGY_EXECUTION_ID) >> [CLOSED_TRADE, OPENED_TRADE]
        1 * tradeService.tryToOpenTrade(tradeOpeningContext) >> Optional.empty()

        and: 'publish correct event'
        1 * eventBus.publishTradeWasNotOpenedEvent(STRATEGY_EXECUTION_ID, SYMBOL_1, EMPTY)

        and: 'no other actions'
        0 * _

        and: 'no trade executions were started'
        strategyExecution.tradeExecutions.isEmpty()
    }

    def 'should complete trade execution successfully'() {
        given: 'trade execution'
        strategyExecution.tradeExecutions << [(SYMBOL_1) : tradeExecution]

        when:
        strategyExecution.closeTrade(OPENED_TRADE, bar)

        then: 'try to close trade'
        1 * tradeService.tryToCloseTrade(tradeClosingContext) >> Optional.of(CLOSED_TRADE)

        and: 'stop trade execution'
        1 * tradeExecution.stop()
        !strategyExecution.tradeExecutions.get(SYMBOL_1)

        and: 'publish correct event'
        1 * eventBus.publishTradeWasClosedEvent(CLOSED_TRADE)

        and: 'no other actions'
        0 * _
    }

    def 'should not complete trade execution if trade was not closed'() {
        given: 'trade execution of the trade'
        strategyExecution.tradeExecutions << [(SYMBOL_1) : tradeExecution]

        when:
        strategyExecution.closeTrade(OPENED_TRADE, bar)

        then: 'try to close trade'
        1 * tradeService.tryToCloseTrade(tradeClosingContext) >> Optional.empty()

        and: 'publish correct event'
        1 * eventBus.publishTradeWasNotClosedEvent(OPENED_TRADE, EMPTY)

        and: 'no other actions'
        0 * _

        and: 'trade execution was not stopped and still exists'
        strategyExecution.tradeExecutions.get(SYMBOL_1) == tradeExecution
    }
}
