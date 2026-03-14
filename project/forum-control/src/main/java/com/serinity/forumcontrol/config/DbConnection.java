package com.serinity.forumcontrol.config;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.nio.file.Path;

import io.github.cdimascio.dotenv.Dotenv;

public final class DbConnection {

  private static final Dotenv dotenv = Dotenv.configure()
      .directory(resolveEnvDirectory())
      .filename(".env")
      .ignoreIfMalformed()
      .ignoreIfMissing()
      .load();

  private static final String URL = resolveAny("DATABASE_URL", "DB_URL");
  private static final String USER = resolveAny("DATABASE_USERNAME", "DB_USER");
  private static final String PASS = resolveAny("DATABASE_PASSWORD", "DB_PASSWORD");

  private static volatile Connection CN;

  private DbConnection() {
  }

  public static Connection getConnection() throws SQLException {
    Connection c = CN;
    if (c == null || c.isClosed()) {
      synchronized (DbConnection.class) {
        c = CN;
        if (c == null || c.isClosed()) {
          CN = c = DriverManager.getConnection(URL, USER, PASS);
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

    throw new IllegalStateException("Missing required environment variable(s): " + String.join(", ", keys));
  }
}
