package com.learnwise.persistence;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MigrationIntegrationTest {
    @Test void initializesFreshDevelopmentSchema() {
        var config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:" + UUID.randomUUID() + ";MODE=MySQL;DB_CLOSE_DELAY=-1");
        config.setUsername("sa");
        try (var dataSource = new HikariDataSource(config)) {
            var result = Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
            assertTrue(result.migrationsExecuted > 0);
        }
    }
}
