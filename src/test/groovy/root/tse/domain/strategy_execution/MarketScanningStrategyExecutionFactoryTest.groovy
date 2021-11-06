package root.tse.domain.strategy_execution

import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory
import root.tse.domain.strategy_execution.trade.TradeService
import spock.lang.Specification

import java.time.Clock
import java.util.concurrent.ScheduledExecutorService

class MarketScanningStrategyExecutionFactoryTest extends Specification {

    private strategyExecutionContext = StrategyExecutionContext.builder().build()
    private marketScanningTaskExecutor = Mock(ScheduledExecutorService)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeService = Mock(TradeService)
    private tradeExecutionFactory = Mock(TradeExecutionFactory)
    private eventBus = Mock(StrategyExecutionEventBus)
    private clock = Mock(Clock)

    private strategyExecutionFactory = new MarketScanningStrategyExecutionFactory(
        marketScanningTaskExecutor, clockSignalDispatcher, tradeService, tradeExecutionFactory, eventBus, clock)

    def 'should create strategy execution correctly'() {
        when:
        def strategyExecution = strategyExecutionFactory.create(strategyExecutionContext)

        then:
        UUID.fromString(strategyExecution.id)
        strategyExecution.context == strategyExecutionContext
        strategyExecution.marketScanningTaskExecutor == marketScanningTaskExecutor
        strategyExecution.clockSignalDispatcher == clockSignalDispatcher
        strategyExecution.tradeService == tradeService
        strategyExecution.tradeExecutionFactory == tradeExecutionFactory
        strategyExecution.eventBus == eventBus
        strategyExecution.clock == clock
        strategyExecution.tradeExecutions.isEmpty()
    }
}
