package root.tse.domain.strategy_execution

import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import spock.lang.Specification

import static StrategyExecutionMode.TRADING
import static root.tse.domain.strategy_execution.trade.TradeType.LONG
import static root.tse.util.TestUtils.*

class StrategyExecutionContextTest extends Specification {

    def 'should provide data correctly'() {
        given:
        def entryRule = Mock(EntryRule)
        def exitRule = Mock(ExitRule)
        def strategy = createStrategy(entryRule, exitRule)
        def strategyExecutionContext = StrategyExecutionContext.builder()
            .strategy(strategy).strategyExecutionMode(TRADING).fundsPerTrade(FUNDS_PER_TRADE)
            .symbols(SYMBOLS).allowedNumberOfSimultaneouslyOpenedTrades(NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES).build()

        expect:
        strategyExecutionContext.getEntryRule() == entryRule
        strategyExecutionContext.getExitRule() == exitRule
        strategyExecutionContext.getTradeType() == LONG
    }

    def 'should throw exception during creation if mandatory data was not provided'() {
        when:
        StrategyExecutionContext.builder().build()

        then:
        def exception = thrown(NullPointerException)
        exception.message.contains('is marked non-null but is null')
    }
}
