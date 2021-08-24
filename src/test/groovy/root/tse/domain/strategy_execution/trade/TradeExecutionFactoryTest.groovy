package root.tse.domain.strategy_execution.trade

import root.tse.domain.strategy_execution.StrategyExecution
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.rule.ExitRule
import spock.lang.Specification

class TradeExecutionFactoryTest extends Specification {

    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeExecutionFactory = new TradeExecutionFactory(clockSignalDispatcher)

    def 'should create trade execution correctly'() {
        given:
        def openedTrade = Trade.builder().build()
        def exitRule = Mock(ExitRule)
        def strategyExecution = Mock(StrategyExecution)

        when:
        def tradeExecution = tradeExecutionFactory.create(openedTrade, strategyExecution)

        then:
        1 * strategyExecution.getExitRule() >> exitRule
        0 * _

        and:
        tradeExecution.openedTrade == openedTrade
        tradeExecution.exitRule == exitRule
        tradeExecution.clockSignalDispatcher == clockSignalDispatcher
        tradeExecution.strategyExecution == strategyExecution
    }
}
