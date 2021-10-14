package root.tse.domain.strategy_execution

import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.event.StrategyExecutionEventBus
import root.tse.domain.strategy_execution.trade.TradeService
import spock.lang.Specification

class SimpleStrategyExecutionFactoryTest extends Specification {

    private strategyExecutionContext = Mock(StrategyExecutionContext)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeService = Mock(TradeService)
    private eventBus = Mock(StrategyExecutionEventBus)

    private strategyExecutionFactory = new SimpleStrategyExecutionFactory(clockSignalDispatcher, tradeService, eventBus)

    def 'should create strategy execution correctly'() {
        when:
        def strategyExecution = strategyExecutionFactory.create(strategyExecutionContext)

        then:
        UUID.fromString(strategyExecution.id)
        strategyExecution.context == strategyExecutionContext
        strategyExecution.clockSignalDispatcher == clockSignalDispatcher
        strategyExecution.tradeService == tradeService
        strategyExecution.eventBus == eventBus
        !strategyExecution.openedTrade
    }
}
