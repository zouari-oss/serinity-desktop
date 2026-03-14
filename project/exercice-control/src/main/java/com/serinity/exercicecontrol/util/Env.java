package com.serinity.exercicecontrol.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.HashMap;
import java.util.Map;

public final class Env {

    private static final Map<String, String> VALUES = new HashMap<>();
    private static boolean LOADED = false;
    private static Path LOADED_FROM = null;

    private Env() {}

    private static void loadIfNeeded() {
        if (LOADED) return;
        LOADED = true;

        // On teste plusieurs emplacements possibles
        String userDir = System.getProperty("user.dir");
        Path[] candidates = new Path[] {
                Paths.get(userDir, ".env"),                 // 1) working dir
                Paths.get(userDir).resolve(".env"),         // (identique, safe)
                Paths.get(userDir, "..", ".env"),           // 2) parent
                Paths.get(userDir, "..", "..", ".env"),     // 3) parent parent
                Paths.get(userDir, "src", "main", ".env"),  // 4) rare
                Paths.get(userDir, "src", "main", "resources", ".env") // 5) si un jour tu le mets là
        };

        Path envPath = null;
        for (Path c : candidates) {
            if (Files.exists(c)) { envPath = c.normalize().toAbsolutePath(); break; }
        }

        if (envPath == null) return;

        try {
            for (String line : Files.readAllLines(envPath, StandardCharsets.UTF_8)) {
                String s = line.trim();
                if (s.isEmpty() || s.startsWith("#")) continue;

                int idx = s.indexOf('=');
                if (idx <= 0) continue;

                String key = s.substring(0, idx).trim();
                String val = s.substring(idx + 1).trim();

                // enlève guillemets
                if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                    val = val.substring(1, val.length() - 1);
                }

                VALUES.put(key, val);
            }
            LOADED_FROM = envPath;
        } catch (IOException ignored) {}
    }

    public static String get(String key) {
        // priorité: variables d'environnement système
        String sys = System.getenv(key);
        if (sys != null && !sys.isBlank()) return sys;

        loadIfNeeded();
        return VALUES.get(key);
    }

    public static String getOrDefault(String key, String def) {
        String v = get(key);
        return (v == null || v.isBlank()) ? def : v;
    }

    // ✅ Debug utile: où il a chargé .env
    public static String debugWhereLoaded() {
        loadIfNeeded();
        return LOADED_FROM == null ? "(not found)" : LOADED_FROM.toString();
    }
}