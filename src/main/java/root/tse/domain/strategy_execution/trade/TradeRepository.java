package root.tse.domain.strategy_execution.trade;

import java.util.Collection;

public interface TradeRepository {

    void save(Trade trade);

    Collection<Trade> getAllTradesByStrategyExecutionId(String strategyExecutionId);
}
