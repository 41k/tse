package root.tse.infrastructure.persistence.signal;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SignalRepository {

    private static final String INSERT_SQL_STATEMENT = "INSERT INTO signals_poc (`provider`, `signals`, `timestamp`) VALUES (?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;

    public void save(String provider, String signals) {
        jdbcTemplate.update(INSERT_SQL_STATEMENT, provider, signals, System.currentTimeMillis());
    }
}
