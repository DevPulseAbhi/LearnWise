package com.learnwise.configuration;

import java.util.Map;

public record AppConfig(String jdbcUrl, String dbUser, String dbPassword, int poolSize, boolean migrate) {
    public static AppConfig fromEnvironment(Map<String, String> environment) {
        return new AppConfig(
                required(environment, "LEARNWISE_DB_URL"),
                required(environment, "LEARNWISE_DB_USER"),
                required(environment, "LEARNWISE_DB_PASSWORD"),
                integer(environment, "LEARNWISE_DB_POOL_SIZE", 10),
                booleanValue(environment, "LEARNWISE_DB_MIGRATE", true));
    }

    private static String required(Map<String, String> env, String key) {
        var value = env.get(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(key + " is required");
        return value;
    }

    private static int integer(Map<String, String> env, String key, int fallback) {
        var value = env.get(key);
        if (value == null || value.isBlank()) return fallback;
        try {
            var parsed = Integer.parseInt(value);
            if (parsed < 1 || parsed > 100) throw new IllegalArgumentException(key + " must be between 1 and 100");
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(key + " must be an integer", exception);
        }
    }

    private static boolean booleanValue(Map<String, String> env, String key, boolean fallback) {
        var value = env.get(key);
        return value == null || value.isBlank() ? fallback : Boolean.parseBoolean(value);
    }
}
