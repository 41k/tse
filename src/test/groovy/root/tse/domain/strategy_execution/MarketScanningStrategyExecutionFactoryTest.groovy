package root.tse.domain.strategy_execution

import root.tse.domain.IdGenerator
import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.event.EventBus
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory
import root.tse.domain.strategy_execution.trade.TradeService
import spock.lang.Specification

import java.time.Clock
import java.util.concurrent.ScheduledExecutorService

import static root.tse.util.TestUtils.STRATEGY_EXECUTION_ID

class MarketScanningStrategyExecutionFactoryTest extends Specification {

    private strategyExecutionContext = StrategyExecutionContext.builder().build()
    private idGenerator = Mock(IdGenerator)
    private marketScanningTaskExecutor = Mock(ScheduledExecutorService)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeService = Mock(TradeService)
    private tradeExecutionFactory = Mock(TradeExecutionFactory)
    private eventBus = Mock(EventBus)
    private clock = Mock(Clock)

    private strategyExecutionFactory = new MarketScanningStrategyExecutionFactory(
        idGenerator, marketScanningTaskExecutor, clockSignalDispatcher, tradeService, tradeExecutionFactory, eventBus, clock)

    def 'should create strategy execution correctly'() {
        when:
        def strategyExecution = strategyExecutionFactory.create(strategyExecutionContext)

        then:
        1 * idGenerator.generateId() >> STRATEGY_EXECUTION_ID
        0 * _

        and:
        strategyExecution.id == STRATEGY_EXECUTION_ID
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
