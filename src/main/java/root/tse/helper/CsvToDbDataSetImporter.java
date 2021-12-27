package root.tse.helper;

import com.opencsv.CSVReader;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import root.tse.domain.clock.Interval;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

// BEFORE RUN target DB Table should be created:
//
//    CREATE TABLE `data_set_1` (
//        `id` int NOT NULL AUTO_INCREMENT,
//        `symbol` varchar(150) NOT NULL,
//        `time_interval` varchar(150) NOT NULL,
//        `timestamp` bigint(20) NOT NULL,
//        `open` double NOT NULL,
//        `high` double NOT NULL,
//        `low` double NOT NULL,
//        `close` double NOT NULL,
//        `volume` double NOT NULL,
//        PRIMARY KEY (`id`)
//    ) ENGINE=InnoDB DEFAULT CHARSET=utf8 AUTO_INCREMENT=1;

//@Component
@RequiredArgsConstructor
public class CsvToDbDataSetImporter implements CommandLineRunner {

    private static final String CSV_FILE_PATH = "csv-file-path";
    private static final int N_LINES_TO_SKIP = 2;
    private static final int TIMESTAMP_MULTIPLIER = 1000;
    private static final String DB_TABLE_NAME = "data_set_1";
    private static final String INTERVAL = Interval.ONE_MINUTE.name();
    private static final String INSERT_QUERY =
        "INSERT INTO " + DB_TABLE_NAME + " (`symbol`, `time_interval`, `open`, `high`, `low`, `close`, `volume`, `timestamp`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final int SYMBOL_INDEX = 2;
    private static final int OPEN_PRICE_INDEX = 3;
    private static final int HIGH_PRICE_INDEX = 4;
    private static final int LOW_PRICE_INDEX = 5;
    private static final int CLOSE_PRICE_INDEX = 6;
    private static final int VOLUME_INDEX = 7;
    private static final int TIMESTAMP_INDEX = 0;

    private final JdbcTemplate jdbcTemplate;

    @Override
    @SneakyThrows
    @Transactional
    public void run(String... args) {
        var streamReader = new InputStreamReader(new FileInputStream(new File(CSV_FILE_PATH)), StandardCharsets.UTF_8);
        try (var csvReader = new CSVReader(streamReader, ',', '"', N_LINES_TO_SKIP)) {
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                jdbcTemplate.update(INSERT_QUERY,
                    line[SYMBOL_INDEX],
                    INTERVAL,
                    line[OPEN_PRICE_INDEX],
                    line[HIGH_PRICE_INDEX],
                    line[LOW_PRICE_INDEX],
                    line[CLOSE_PRICE_INDEX],
                    line[VOLUME_INDEX],
                    timestamp(line));
            }
        }
        System.out.println(">>> Data import has been completed.");
    }

    private long timestamp(String[] csvLine) {
        return Long.parseLong(csvLine[TIMESTAMP_INDEX]) * TIMESTAMP_MULTIPLIER;
    }
}
