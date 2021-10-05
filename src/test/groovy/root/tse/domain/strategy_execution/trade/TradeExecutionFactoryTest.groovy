package root.tse.domain.strategy_execution.trade

import root.tse.domain.strategy_execution.StrategyExecution
import root.tse.domain.strategy_execution.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.rule.ExitRule
import spock.lang.Specification

import static root.tse.util.TestData.OPENED_TRADE

class TradeExecutionFactoryTest extends Specification {

    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeExecutionFactory = new TradeExecutionFactory(clockSignalDispatcher)

    def 'should create trade execution correctly'() {
        given:
        def exitRule = Mock(ExitRule)
        def strategyExecution = Mock(StrategyExecution)

        when:
        def tradeExecution = tradeExecutionFactory.create(OPENED_TRADE, strategyExecution)

        then:
        1 * strategyExecution.getExitRule() >> exitRule
        0 * _

        and:
        tradeExecution.openedTrade == OPENED_TRADE
        tradeExecution.exitRule == exitRule
        tradeExecution.clockSignalDispatcher == clockSignalDispatcher
        tradeExecution.strategyExecution == strategyExecution
    }
}
