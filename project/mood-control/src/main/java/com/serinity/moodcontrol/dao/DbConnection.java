package com.serinity.moodcontrol.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public final class DbConnection {

    private static final Dotenv dotenv = Dotenv.configure()
            .directory("mood-control")
            .filename(".env")
            .ignoreIfMalformed()
            .ignoreIfMissing()
            .load();

    private static final String URL  = dotenv.get("DATABASE_URL");
    private static final String USER = dotenv.get("DATABASE_USERNAME");
    private static final String PASS = dotenv.get("DATABASE_PASSWORD");

    // Single shared Connection (singleton)
    private static volatile Connection CN;

    private DbConnection() {
    }

    /**
     * Real singleton connection:
     * - first call opens it
     * - later calls reuse the same connection
     * - DO NOT close it in DAOs
     */
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

    /** Call once on app shutdown (JavaFX stop()). */
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
}
