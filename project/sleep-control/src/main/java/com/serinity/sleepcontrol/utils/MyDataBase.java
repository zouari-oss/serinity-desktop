package com.serinity.sleepcontrol.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

    private static MyDataBase instance;
    private final Connection connection;

    private static final String URL  =
            "jdbc:mysql://127.0.0.1:3307/serinity_sleep?useSSL=false&serverTimezone=UTC";
    private static final String USER = "root";
    private static final String PASS = "";

    private MyDataBase() {
        Connection tmp = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // driver MySQL 8+
            tmp = DriverManager.getConnection(URL, USER, PASS);
            System.out.println("Connected to DB");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL Driver introuvable : " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("Erreur connexion DB : " + e.getMessage());
        }
        this.connection = tmp;
    }

    public static synchronized MyDataBase getInstance() {
        if (instance == null) {
            instance = new MyDataBase();
        }
        return instance;
    }

    public Connection getConnection() {
        return connection;
    }
}
