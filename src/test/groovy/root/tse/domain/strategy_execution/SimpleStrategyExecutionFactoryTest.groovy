package root.tse.domain.strategy_execution

import root.tse.domain.IdGenerator
import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.event.EventBus
import root.tse.domain.strategy_execution.trade.TradeService
import spock.lang.Specification

import static root.tse.util.TestUtils.STRATEGY_EXECUTION_ID

class SimpleStrategyExecutionFactoryTest extends Specification {

    private strategyExecutionContext = StrategyExecutionContext.builder().build()
    private idGenerator = Mock(IdGenerator)
    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeService = Mock(TradeService)
    private eventBus = Mock(EventBus)

    private strategyExecutionFactory =
        new SimpleStrategyExecutionFactory(idGenerator, clockSignalDispatcher, tradeService, eventBus)

    def 'should create strategy execution correctly'() {
        when:
        def strategyExecution = strategyExecutionFactory.create(strategyExecutionContext)

        then:
        1 * idGenerator.generateId() >> STRATEGY_EXECUTION_ID
        0 * _

        and:
        strategyExecution.id == STRATEGY_EXECUTION_ID
        strategyExecution.context == strategyExecutionContext
        strategyExecution.clockSignalDispatcher == clockSignalDispatcher
        strategyExecution.tradeService == tradeService
        strategyExecution.eventBus == eventBus
        !strategyExecution.openedTrade
    }
}
