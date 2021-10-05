package root.tse.domain.strategy_execution

import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus
import root.tse.domain.strategy_execution.trade.OrderExecutor
import root.tse.domain.strategy_execution.trade.TradeExecutionFactory
import root.tse.domain.strategy_execution.trade.TradeRepository
import spock.lang.Specification

import java.util.concurrent.ScheduledExecutorService

class StrategyExecutionFactoryTest extends Specification {

    private strategyExecutionContext = Mock(StrategyExecutionContext)
    private marketScanningTaskExecutor = Mock(ScheduledExecutorService)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private orderExecutor = Mock(OrderExecutor)
    private tradeExecutionFactory = Mock(TradeExecutionFactory)
    private tradeRepository = Mock(TradeRepository)
    private eventBus = Mock(StrategyExecutionEventBus)

    private strategyExecutionFactory = new StrategyExecutionFactory(
        marketScanningTaskExecutor, clockSignalDispatcher, orderExecutor, tradeExecutionFactory, tradeRepository, eventBus)

    def 'should create strategy execution correctly'() {
        when:
        def strategyExecution = strategyExecutionFactory.create(strategyExecutionContext)

        then:
        UUID.fromString(strategyExecution.id)
        strategyExecution.context == strategyExecutionContext
        strategyExecution.marketScanningTaskExecutor == marketScanningTaskExecutor
        strategyExecution.clockSignalDispatcher == clockSignalDispatcher
        strategyExecution.orderExecutor == orderExecutor
        strategyExecution.tradeExecutionFactory == tradeExecutionFactory
        strategyExecution.tradeRepository == tradeRepository
        strategyExecution.eventBus == eventBus
        strategyExecution.tradeExecutions.isEmpty()
    }
}
