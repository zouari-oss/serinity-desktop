package com.serinity.consultationcontrol.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.nio.file.*;

public class Mydatabase {
    private static Mydatabase instance;
    private final Connection connection;

    private Mydatabase() {
        try {
            Class.forName("org.mariadb.jdbc.Driver");
            Map<String, String> env = loadEnvFile();

            String url = resolveAny(env, "DATABASE_URL", "DB_URL");
            String user = resolveAny(env, "DATABASE_USERNAME", "DB_USER");
            String password = resolveAny(env, "DATABASE_PASSWORD", "DB_PASSWORD");

            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize consultation DB connection", e);
        }
    }

    public static Mydatabase getInstance(){
        if(instance == null) {
            instance = new Mydatabase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private static Map<String, String> loadEnvFile() {
        Map<String, String> values = new HashMap<>();
        Path envFile = resolveEnvFile();
        if (envFile == null) {
            return values;
        }

        try {
            List<String> lines = Files.readAllLines(envFile);
            for (String line : lines) {
                String trimmed = line.trim();
                if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
                    continue;
                }

                int idx = trimmed.indexOf('=');
                String key = trimmed.substring(0, idx).trim();
                String value = trimmed.substring(idx + 1);
                values.put(key, sanitizeValue(value));
            }
        } catch (IOException ignored) {
        }

        return values;
    }

    private static Path resolveEnvFile() {
        String userDir = System.getProperty("user.dir");
        final Path[] candidates = {
                Paths.get(userDir, ".env"),                 // 1) working dir
                Paths.get(userDir).resolve(".env"),         // (identique, safe)
                Paths.get(userDir, "..", ".env"),           // 2) parent
                Paths.get(userDir, "..", "..", ".env"),     // 3) parent parent
                Paths.get(userDir, "src", "main", ".env"),  // 4) rare
                Paths.get(userDir, "src", "main", "resources", ".env") // 5) si un jour tu le mets là
        };

        for (Path candidate : candidates) {
            if (candidate != null && Files.exists(candidate)) {
                return candidate;
            }
        }

        return null;
    }

    private static String resolveAny(final Map<String, String> env, final String... keys) {
        for (final String key : keys) {
            final String fromProcess = System.getenv(key);
            if (fromProcess != null && !fromProcess.isBlank()) {
                return sanitizeValue(fromProcess);
            }

            if (env.containsKey(key)) {
                return sanitizeValue(env.get(key));
            }
        }

        throw new IllegalStateException("Missing required environment variable(s): " + String.join(", ", keys));
    }

    private static String sanitizeValue(final String value) {
        if (value == null) {
            return null;
        }

        String sanitized = value.trim();
        sanitized = stripWrappingQuotes(sanitized);
        return sanitized;
    }

    private static String stripWrappingQuotes(final String value) {
        String result = value;

        while (!result.isEmpty() && (result.startsWith("\"") || result.startsWith("'"))) {
            result = result.substring(1).trim();
        }

        while (!result.isEmpty() && (result.endsWith("\"") || result.endsWith("'"))) {
            result = result.substring(0, result.length() - 1).trim();
        }

        while (result.startsWith("\\\"") || result.startsWith("\\'")) {
            result = result.substring(2).trim();
        }

        while (result.endsWith("\\\"") || result.endsWith("\\'")) {
            result = result.substring(0, result.length() - 2).trim();
        }

        return result;
    }
}
