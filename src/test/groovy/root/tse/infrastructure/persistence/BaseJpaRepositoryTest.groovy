package root.tse.infrastructure.persistence

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.jdbc.JdbcTestUtils
import root.TseApp
import spock.lang.Specification

@SpringBootTest(classes = TseApp.class)
abstract class BaseJpaRepositoryTest extends Specification {

    @Autowired
    private JdbcTemplate jdbcTemplate

    protected cleanTable(String tableName) {
        JdbcTestUtils.deleteFromTables(jdbcTemplate, tableName)
    }
}
