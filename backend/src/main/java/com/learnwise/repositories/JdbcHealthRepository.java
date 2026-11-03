package com.learnwise.repositories;

import javax.sql.DataSource;
import java.sql.SQLException;

public final class JdbcHealthRepository {
    private final DataSource dataSource;
    public JdbcHealthRepository(DataSource dataSource) { this.dataSource = dataSource; }
    public boolean isDatabaseAvailable() {
        try (var connection = dataSource.getConnection();
             var statement = connection.prepareStatement("SELECT 1");
             var results = statement.executeQuery()) {
            return results.next() && results.getInt(1) == 1;
        } catch (SQLException exception) {
            return false;
        }
    }
}
