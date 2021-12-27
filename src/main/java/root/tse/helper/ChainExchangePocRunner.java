package root.tse.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import root.tse.domain.clock.Interval;

import java.util.HashMap;
import java.util.Map;

//@Component
@RequiredArgsConstructor
public class ChainExchangePocRunner implements CommandLineRunner {

    private static final long START_TIMESTAMP = 1609459260000L;
    private static final long END_TIMESTAMP = 1640679540000L;
    private static final long TIMESTAMP_INCREMENT = Interval.ONE_MINUTE.inMillis();
    private static final double USD_AMOUNT = 1000d;

    private static final String DATA_SET_TABLE_NAME = "bitstamp_1m_data_set";
    private static final String PRICES_QUERY_FORMAT =
        "SELECT * FROM " + DATA_SET_TABLE_NAME + " WHERE symbol = '%s' AND `timestamp` >= %s AND `timestamp` <= %s";

    private static final String POC_RESULT_TABLE_NAME = "chain_exchange_poc_result_5";
    private static final String INSERT_RESULT_QUERY = "INSERT INTO " + POC_RESULT_TABLE_NAME + " (`result`, `timestamp`) VALUES (?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final Map<String, Map<Long, Double>> dataSet;

    @Override
    @Transactional
    public void run(String... args) {
        initDataSet();
        int processedTimestamps = 0;
        for (var timestamp = START_TIMESTAMP; timestamp <= END_TIMESTAMP; timestamp += TIMESTAMP_INCREMENT) {
            var ltcUsdPrice = dataSet.get("LTC/USD").get(timestamp);
            var ltcBtcPrice = dataSet.get("LTC/BTC").get(timestamp);
            var btcUsdPrice = dataSet.get("BTC/USD").get(timestamp);
            var ltcAmount = USD_AMOUNT / ltcUsdPrice; // buy LTC for USD
            var btcAmount = ltcAmount * ltcBtcPrice; // sell LTC for BTC
            var resultUsdAmount = btcAmount * btcUsdPrice; // sell BTC for USD
            save(resultUsdAmount, timestamp);
            processedTimestamps++;
            if (processedTimestamps % 10_000 == 0) {
                System.out.println(">>> [" + processedTimestamps + "] have been processed");
            }
        }
        System.out.println(">>> PoC has been finished.");
    }

    private void save(Double result, Long timestamp) {
        jdbcTemplate.update(INSERT_RESULT_QUERY, result, timestamp);
    }

    private void initDataSet() {
        initDataSet("LTC/USD");
        initDataSet("LTC/BTC");
        initDataSet("BTC/USD");
    }

    private void initDataSet(String symbol) {
        var timestampToPriceMap = dataSet.computeIfAbsent(symbol, (ignore) -> new HashMap<>());
        var query = String.format(PRICES_QUERY_FORMAT, symbol, START_TIMESTAMP, END_TIMESTAMP);
        var rowSet = jdbcTemplate.queryForRowSet(query);
        while (rowSet.next()) {
            var timestamp = rowSet.getLong("timestamp");
            var price = rowSet.getDouble("close");
            timestampToPriceMap.put(timestamp, price);
        }
    }
}
