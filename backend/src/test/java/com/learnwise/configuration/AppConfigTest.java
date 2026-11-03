package com.learnwise.configuration;

import org.junit.jupiter.api.Test;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

class AppConfigTest {
    @Test void rejectsMissingCredentials() {
        var exception = assertThrows(IllegalArgumentException.class,
                () -> AppConfig.fromEnvironment(Map.of("LEARNWISE_DB_URL", "jdbc:test")));
        assertTrue(exception.getMessage().contains("LEARNWISE_DB_USER"));
    }

    @Test void appliesSafePoolDefaults() {
        var config = AppConfig.fromEnvironment(Map.of(
                "LEARNWISE_DB_URL", "jdbc:test", "LEARNWISE_DB_USER", "u", "LEARNWISE_DB_PASSWORD", "p"));
        assertEquals(10, config.poolSize());
        assertTrue(config.migrate());
    }
}
