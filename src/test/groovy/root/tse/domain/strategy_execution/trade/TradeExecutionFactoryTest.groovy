package root.tse.domain.strategy_execution.trade

import root.tse.domain.clock.ClockSignalDispatcher
import root.tse.domain.strategy_execution.MarketScanningStrategyExecution
import root.tse.domain.rule.ExitRule
import spock.lang.Specification

import static root.tse.util.TestUtils.OPENED_TRADE

class TradeExecutionFactoryTest extends Specification {

    private clockSignalDispatcher = Mock(ClockSignalDispatcher)
    private tradeExecutionFactory = new TradeExecutionFactory(clockSignalDispatcher)

    def 'should create trade execution correctly'() {
        given:
        def exitRule = Mock(ExitRule)
        def strategyExecution = Mock(MarketScanningStrategyExecution)

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
