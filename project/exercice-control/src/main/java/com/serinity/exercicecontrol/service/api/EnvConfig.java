package com.serinity.exercicecontrol.service.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public final class EnvConfig {

    private static final Map<String, String> ENV = new HashMap<>();

    static {
        loadDotEnv();
    }

    private EnvConfig() {
        // util class
    }

    // =========================
    // PUBLIC API
    // =========================

    public static String get(String key) {
        if (key == null || key.isBlank()) return null;

        // priorité: .env
        if (ENV.containsKey(key)) {
            return ENV.get(key);
        }

        // fallback variable système
        String sys = System.getenv(key);
        if (sys != null) return sys;

        // fallback JVM property
        return System.getProperty(key);
    }

    public static String get(String key, String defaultValue) {
        String v = get(key);
        return (v == null || v.isBlank()) ? defaultValue : v;
    }

    public static String require(String key) {
        String v = get(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException(
                    "Missing config: " + key + " (set env var or .env)"
            );
        }
        return v;
    }

    public static int getInt(String key, int defaultValue) {
        String v = get(key);
        if (v == null || v.isBlank()) return defaultValue;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double getDouble(String key, double defaultValue) {
        String v = get(key);
        if (v == null || v.isBlank()) return defaultValue;
        try {
            return Double.parseDouble(v.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        String v = get(key);
        if (v == null || v.isBlank()) return defaultValue;

        String s = v.trim().toLowerCase();
        return switch (s) {
            case "true", "1", "yes", "y", "on" -> true;
            case "false", "0", "no", "n", "off" -> false;
            default -> defaultValue;
        };
    }

    public static boolean has(String key) {
        String v = get(key);
        return v != null && !v.isBlank();
    }

    // =========================
    // .env LOADER
    // =========================

    private static void loadDotEnv() {
        try {
            File file = new File(".env");

            if (!file.exists() || !file.isFile()) {
                System.out.println(".env not found at project root.");
                return;
            }

            try (BufferedReader br = new BufferedReader(
                    new FileReader(file, StandardCharsets.UTF_8))) {

                String line;
                while ((line = br.readLine()) != null) {

                    line = line.trim();

                    // ignore commentaires et lignes vides
                    if (line.isEmpty() || line.startsWith("#")) continue;

                    int idx = line.indexOf('=');
                    if (idx <= 0) continue;

                    String key = line.substring(0, idx).trim();
                    String value = line.substring(idx + 1).trim();

                    // retire guillemets éventuels
                    if ((value.startsWith("\"") && value.endsWith("\"")) ||
                            (value.startsWith("'") && value.endsWith("'"))) {
                        value = value.substring(1, value.length() - 1);
                    }

                    ENV.put(key, value);
                }
            }

            System.out.println(".env loaded successfully.");

        } catch (Exception e) {
            System.err.println("Failed to load .env: " + e.getMessage());
        }
    }
}