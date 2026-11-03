package com.learnwise.configuration;

import java.util.Map;

public record AuthConfig(String frontendOrigin, int sessionTtlMinutes, boolean secureCookie) {
    public static AuthConfig fromEnvironment(Map<String, String> env) {
        return new AuthConfig(required(env, "LEARNWISE_FRONTEND_ORIGIN"),
                integer(env.get("LEARNWISE_SESSION_TTL_MINUTES"), 60),
                Boolean.parseBoolean(env.getOrDefault("LEARNWISE_COOKIE_SECURE", "false")));
    }
    private static String required(Map<String, String> env, String key) {
        var value = env.get(key);
        if (value == null || value.isBlank()) throw new IllegalArgumentException(key + " is required");
        return value;
    }
    private static int integer(String value, int fallback) {
        if (value == null || value.isBlank()) return fallback;
        try {
            var parsed = Integer.parseInt(value);
            if (parsed < 5 || parsed > 1440) throw new IllegalArgumentException("LEARNWISE_SESSION_TTL_MINUTES must be between 5 and 1440");
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("LEARNWISE_SESSION_TTL_MINUTES must be an integer", exception);
        }
    }
}
