package com.serinity.exercicecontrol.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    private static DbConnection instance;
    private Connection connection;

    // Change these defaults to match your DB if you want
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/serinity?useUnicode=true&characterEncoding=UTF-8&serverTimezone=UTC";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "";

    private DbConnection() {
        try {
            String url = getEnv("DB_URL", DEFAULT_URL);
            String user = getEnv("DB_USER", DEFAULT_USER);
            String pass = getEnv("DB_PASSWORD", DEFAULT_PASS);


            connection = DriverManager.getConnection(url, user, pass);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion BD: " + e.getMessage(), e);
        }
    }

    public static DbConnection getInstance() {
        if (instance == null) {
            instance = new DbConnection();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }

    private static String getEnv(String key, String fallback) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? fallback : v;
    }
}
