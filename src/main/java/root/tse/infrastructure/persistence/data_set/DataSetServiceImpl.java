package root.tse.infrastructure.persistence.data_set;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ta4j.core.Bar;
import org.ta4j.core.BarSeries;
import org.ta4j.core.BaseBarSeries;
import root.tse.domain.backtest.DataSetService;
import root.tse.domain.strategy_execution.Interval;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@RequiredArgsConstructor
public class DataSetServiceImpl implements DataSetService {

    private static final String TIMESTAMP_QUERY_FORMAT = "SELECT %s(timestamp) FROM %s WHERE symbol='%s'";

    private final JdbcTemplate jdbcTemplate;
    private final BarRowMapper barRowMapper;
    private final Map<String, Map<String, Map<Interval, Map<Long, Bar>>>> dataSetStore = new HashMap<>();

    @Override
    public Long getStartTimestamp(String dataSetName, String symbol) {
        var query = String.format(TIMESTAMP_QUERY_FORMAT, "MIN", dataSetName, symbol);
        return jdbcTemplate.queryForObject(query, Long.class);
    }

    @Override
    public Long getEndTimestamp(String dataSetName, String symbol) {
        var query = String.format(TIMESTAMP_QUERY_FORMAT, "MAX", dataSetName, symbol);
        return jdbcTemplate.queryForObject(query, Long.class);
    }

    @Override
    public BarSeries getSeries(String dataSetName, String symbol, Interval interval, Long untilTimestamp, Integer seriesLength) {
        var bars = getDataSet(dataSetName, symbol, interval).entrySet().stream()
            .filter(entry -> entry.getKey() <= untilTimestamp)
            .map(Map.Entry::getValue)
            .limit(seriesLength)
            .sorted(Comparator.comparing(Bar::getEndTime))
            .collect(toList());
        return new BaseBarSeries(bars);
    }

    private Map<Long, Bar> getDataSet(String dataSetName, String symbol, Interval interval) {
        return dataSetStore
            .computeIfAbsent(dataSetName, (ignore) -> new HashMap<>())
            .computeIfAbsent(symbol, (ignore) -> new HashMap<>())
            .computeIfAbsent(interval, (ignore) -> buildDataSet(dataSetName, symbol, interval));
    }

    private Map<Long, Bar> buildDataSet(String dataSetName, String symbol, Interval interval) {
        var query = String.format(
            "SELECT * FROM %s WHERE symbol = '%s' AND time_interval = '%s' ORDER BY id DESC",
            dataSetName, symbol, interval.name());
        var bars = jdbcTemplate.query(query, barRowMapper);
        return bars.stream().collect(toMap(
            bar -> bar.getEndTime().toInstant().toEpochMilli(),
            Function.identity(),
            (v1,v2)->v1,
            LinkedHashMap::new)
        );
    }
}
