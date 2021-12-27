package root.tse.domain.backtest;

import org.ta4j.core.BarSeries;
import root.tse.domain.clock.Interval;
import root.tse.domain.order.OrderType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface DataSetService {

    Long getStartTimestamp(String dataSetName, String symbol);

    Long getEndTimestamp(String dataSetName, String symbol);

    BarSeries getSeries(String dataSetName, String symbol, Interval interval, Long untilTimestamp, Integer seriesLength);

    Optional<Map<String, Map<OrderType, Double>>> getCurrentPrices(String dataSetName, List<String> symbols, Long currentTimestamp);
}
