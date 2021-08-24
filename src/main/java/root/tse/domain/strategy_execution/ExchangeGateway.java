package root.tse.domain.strategy_execution;

import org.ta4j.core.BarSeries;
import root.tse.domain.strategy_execution.trade.Order;

import java.util.Optional;

public interface ExchangeGateway {

    Optional<BarSeries> getSeries(String symbol, Interval interval, Integer seriesLength);

    Order execute(Order order);
}
