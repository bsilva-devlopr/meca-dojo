package br.com.mecadojo.api.foundation;

import static org.assertj.core.api.Assertions.assertThat;

import br.com.mecadojo.api.support.AbstractPostgreSqlTestcontainer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
class PostgreSqlFoundationIntegrationTests extends AbstractPostgreSqlTestcontainer {

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Test
    void connectsToPostgreSqlAndRunsFlywayMigration() {
        Integer markerCount = jdbcTemplate.queryForObject(
            "select count(*) from foundation_schema_marker where id = 1",
            Integer.class
        );
        Integer migrationCount = jdbcTemplate.queryForObject(
            "select count(*) from flyway_schema_history where script = 'V1__foundation.sql' and success = true",
            Integer.class
        );

        assertThat(markerCount).isEqualTo(1);
        assertThat(migrationCount).isEqualTo(1);
    }
}
