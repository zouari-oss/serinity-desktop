package com.serinity.exercicecontrol.dao;

import com.serinity.exercicecontrol.util.Env;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbConnection {

    private static DbConnection instance;
    private Connection connection;


    private static final String DEFAULT_URL = "jdbc:mariadb://localhost:3306/serinity_test";
    private static final String DEFAULT_USER = "root";
    private static final String DEFAULT_PASS = "root";
    private static final String DEFAULT_DRIVER = "org.mariadb.jdbc.Driver";

    private DbConnection() {
        try {
            String driver = firstNonBlank(
                    Env.get("JDBC_DRIVER"),
                    DEFAULT_DRIVER
            );
            String url = firstNonBlank(
                    Env.get("DB_URL"),
                    Env.get("DATABASE_URL"),
                    DEFAULT_URL
            );
            String user = firstNonBlank(
                    Env.get("DB_USER"),
                    Env.get("DATABASE_USERNAME"),
                    DEFAULT_USER
            );
            String pass = firstNonBlank(
                    Env.get("DB_PASSWORD"),
                    Env.get("DATABASE_PASSWORD"),
                    DEFAULT_PASS
            );

            Class.forName(driver);
            connection = DriverManager.getConnection(url, user, pass);

        } catch (SQLException e) {
            throw new RuntimeException("Erreur connexion BD: " + e.getMessage(), e);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("JDBC driver introuvable: " + e.getMessage(), e);
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

    private static String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
