package root.tse.domain.strategy_execution

import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import spock.lang.Specification

import static root.tse.domain.strategy_execution.trade.TradeType.LONG
import static root.tse.util.TestData.*
import static root.tse.util.TestUtils.createStrategy
import static StrategyExecutionMode.TRADING

class StrategyExecutionContextTest extends Specification {

    private entryRule = Mock(EntryRule)
    private exitRule = Mock(ExitRule)
    private strategy = createStrategy(entryRule, exitRule)

    private strategyExecutionContext = StrategyExecutionContext.builder().strategy(strategy).symbols(SYMBOLS).executionMode(TRADING)
        .allowedNumberOfSimultaneouslyOpenedTrades(NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES).fundsPerTrade(FUNDS_PER_TRADE).build()

    def 'should provide data correctly'() {
        expect:
        strategyExecutionContext.getEntryRule() == entryRule
        strategyExecutionContext.getExitRule() == exitRule
        strategyExecutionContext.getTradeType() == LONG
    }
}
