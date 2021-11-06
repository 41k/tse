package root.tse.domain.backtest;

import org.ta4j.core.BarSeries;
import root.tse.domain.strategy_execution.Interval;

public interface DataSetService {

    Long getStartTimestamp(String dataSetName, String symbol);

    Long getEndTimestamp(String dataSetName, String symbol);

    BarSeries getSeries(String dataSetName, String symbol, Interval interval, Long untilTimestamp, Integer seriesLength);
}
