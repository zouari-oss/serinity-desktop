package com.serinity.moodcontrol.dao;

import io.github.cdimascio.dotenv.Dotenv;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton JDBC connection helper for the mood-control module.
 *
 * <p>
 * Reads database credentials from a {@code .env} file in the working
 * directory (keys: {@code DB_URL}, {@code DB_USER}, {@code DB_PASSWORD}).
 * The connection is created lazily on the first call to
 * {@link #getConnection()} and reused afterwards.  A new connection is
 * opened automatically if the cached one is closed or invalid.
 * </p>
 *
 * <p>
 * <strong>Note:</strong> do not close the connection returned by
 * {@link #getConnection()} from application code; it is intentionally
 * long-lived.  Integration tests that need full control may close it and the
 * next call will reconnect.
 * </p>
 */
public final class DbConnection {

    private static final Dotenv DOTENV = Dotenv.configure()
            .directory(".")
            .filename(".env")
            .ignoreIfMissing()
            .load();

    private static Connection instance;

    private DbConnection() {
    }

    /**
     * Returns the shared {@link Connection}, opening one if necessary.
     *
     * @return an open {@link Connection}
     * @throws SQLException if a database access error occurs
     */
    public static synchronized Connection getConnection() throws SQLException {
        if (instance == null || instance.isClosed()) {
            final String url      = DOTENV.get("DB_URL");
            final String user     = DOTENV.get("DB_USER");
            final String password = DOTENV.get("DB_PASSWORD");
            instance = DriverManager.getConnection(url, user, password);
        }
        return instance;
    }
} // DbConnection final class
