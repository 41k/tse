package root.tse.domain.strategy_execution;

import root.tse.domain.strategy_execution.rule.EntryRule;
import root.tse.domain.strategy_execution.rule.ExitRule;
import root.tse.domain.strategy_execution.trade.TradeType;

// Note: it is necessary to inject instance of ExchangeGateway into
// Strategy implementation in order to be able to build Entry/Exit rules
// based on different symbols and intervals inside the Strategy implementation
public interface Strategy {

    String getId();

    String getName();

    TradeType getTradeType();

    EntryRule getEntryRule();

    ExitRule getExitRule();
}
