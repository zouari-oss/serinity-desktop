package com.serinity.forumcontrol.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.github.cdimascio.dotenv.Dotenv;

public final class DbConnection {

  private static final Dotenv dotenv = Dotenv.configure()
      .directory(resolveEnvDirectory())
      .filename(".env")
      .ignoreIfMalformed()
      .ignoreIfMissing()
      .load();

  private static volatile Connection CN;
  private static volatile Settings settings;

  private DbConnection() {
  }

  public static Connection getConnection() throws SQLException {
    Connection c = CN;
    if (c == null || c.isClosed()) {
      synchronized (DbConnection.class) {
        c = CN;
        if (c == null || c.isClosed()) {
          Settings cfg = settings();
          CN = c = DriverManager.getConnection(cfg.url(), cfg.user(), cfg.pass());
        }
      }
    }
    return c;
  }

  public static void close() {
    synchronized (DbConnection.class) {
      if (CN != null) {
        try {
          CN.close();
        } catch (SQLException ignored) {
        } finally {
          CN = null;
        }
      }
    }
  }

  private static String resolveEnvDirectory() {
    final String configured = System.getProperty("serinity.env.dir");
    if (configured != null && !configured.isBlank()) {
      return configured;
    }

    final String appPath = System.getProperty("jpackage.app-path");
    if (appPath != null && !appPath.isBlank()) {
      final Path binPath = Path.of(appPath).toAbsolutePath().normalize().getParent();
      if (binPath != null) {
        final Path appDir = binPath.getParent();
        if (appDir != null) {
          return appDir.toString();
        }
      }
    }

    final Path cwd = Path.of(".").toAbsolutePath().normalize();
    if (java.nio.file.Files.exists(cwd.resolve(".env"))) {
      return cwd.toString();
    }

    final Path parent = cwd.getParent();
    if (parent != null && java.nio.file.Files.exists(parent.resolve(".env"))) {
      return parent.toString();
    }

    return ".";
  }

  private static Settings settings() throws SQLException {
    Settings current = settings;
    if (current != null) {
      return current;
    }

    synchronized (DbConnection.class) {
      current = settings;
      if (current == null) {
        current = new Settings(
            requireAny("DATABASE_URL", "DB_URL"),
            requireAny("DATABASE_USERNAME", "DB_USER"),
            requireAny("DATABASE_PASSWORD", "DB_PASSWORD"));
        settings = current;
      }
    }
    return current;
  }

  private static String requireAny(final String... keys) throws SQLException {
    String value = resolveAny(keys);
    if (value != null) {
      return value;
    }

    List<String> missing = new ArrayList<>();
    for (String key : keys) {
      missing.add(key);
    }
    throw new SQLException("Missing required environment variable(s): " + String.join(", ", missing));
  }

  private static String resolveAny(final String... keys) {
    for (final String key : keys) {
      final String fromDotenv = dotenv.get(key);
      if (fromDotenv != null && !fromDotenv.isBlank()) {
        return fromDotenv;
      }

      final String fromEnv = System.getenv(key);
      if (fromEnv != null && !fromEnv.isBlank()) {
        return fromEnv;
      }
    }

    return null;
  }

  private record Settings(String url, String user, String pass) {
  }
}
