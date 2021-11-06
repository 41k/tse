package root.tse.infrastructure.persistence.data_set;

import org.springframework.jdbc.core.RowMapper;
import org.ta4j.core.Bar;
import org.ta4j.core.BaseBar;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

public class BarRowMapper implements RowMapper<Bar> {

    @Override
    public Bar mapRow(ResultSet resultSet, int i) throws SQLException {
        var duration = Duration.ZERO;
        var closeTimestamp = resultSet.getLong("timestamp");
        var zonedCloseTime = ZonedDateTime.ofInstant(Instant.ofEpochMilli(closeTimestamp), ZoneId.systemDefault());
        var open = String.valueOf(resultSet.getDouble("open"));
        var high = String.valueOf(resultSet.getDouble("high"));
        var low = String.valueOf(resultSet.getDouble("low"));
        var close = String.valueOf(resultSet.getDouble("close"));
        var volume = String.valueOf(resultSet.getDouble("volume"));
        return new BaseBar(duration, zonedCloseTime, open, high, low, close, volume);
    }
}
