package com.learnwise.configuration;

import java.nio.file.Path;
import java.util.Map;

public record DocumentConfig(Path storageRoot, long maxBytes, int chunkSize, int chunkOverlap) {
    public static DocumentConfig fromEnvironment(Map<String, String> env) {
        var root = env.getOrDefault("LEARNWISE_DOCUMENT_STORAGE_PATH", "data/documents");
        var max = number(env.get("LEARNWISE_DOCUMENT_MAX_BYTES"), 10L * 1024 * 1024, 1024, 100L * 1024 * 1024);
        var size = (int) number(env.get("LEARNWISE_DOCUMENT_CHUNK_SIZE"), 1800, 200, 10000);
        var overlap = (int) number(env.get("LEARNWISE_DOCUMENT_CHUNK_OVERLAP"), 250, 0, size - 1);
        return new DocumentConfig(Path.of(root).toAbsolutePath().normalize(), max, size, overlap);
    }

    private static long number(String value, long fallback, long min, long max) {
        if (value == null || value.isBlank()) return fallback;
        try {
            var parsed = Long.parseLong(value);
            if (parsed < min || parsed > max) throw new IllegalArgumentException("Document configuration value is out of range");
            return parsed;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Document configuration value must be numeric", exception);
        }
    }
}
