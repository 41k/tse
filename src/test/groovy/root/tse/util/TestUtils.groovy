package root.tse.util

import root.tse.domain.strategy_execution.Strategy
import root.tse.domain.strategy_execution.rule.EntryRule
import root.tse.domain.strategy_execution.rule.ExitRule
import root.tse.domain.strategy_execution.trade.TradeType

import static root.tse.domain.strategy_execution.trade.TradeType.LONG

class TestUtils {

    public static Strategy createStrategy(EntryRule entryRule, ExitRule exitRule) {
        new Strategy() {
            @Override
            String getId() {
                return '1'
            }
            @Override
            String getName() {
                return 'strategy-1'
            }
            @Override
            TradeType getTradeType() {
                return LONG
            }
            @Override
            EntryRule getEntryRule() {
                return entryRule
            }
            @Override
            ExitRule getExitRule() {
                return exitRule
            }
        }
    }
}
