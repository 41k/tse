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
            .transactionFeePercent(TRANSACTION_FEE_PERCENT).symbols(SYMBOLS)
            .allowedNumberOfSimultaneouslyOpenedTrades(NUMBER_OF_SIMULTANEOUSLY_OPENED_TRADES).build()

        expect:
        strategyExecutionContext.getEntryRule() == entryRule
        strategyExecutionContext.getExitRule() == exitRule
        strategyExecutionContext.getTradeType() == LONG
    }

    def 'should init allowedNumberOfSimultaneouslyOpenedTrades = 1 by default'() {
        expect:
        StrategyExecutionContext.builder().build().allowedNumberOfSimultaneouslyOpenedTrades == 1
    }
}
