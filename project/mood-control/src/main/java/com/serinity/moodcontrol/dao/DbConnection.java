package com.serinity.moodcontrol.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DbConnection {

    private DbConnection() {}

    // Change these if needed (XAMPP defaults often root + empty password)
    // private static final String URL = "jdbc:mariadb://localhost:3306/serinity";
    //private static final String USER = "root";
    //private static final String PASS = "";

    //public static Connection getConnection() throws SQLException {
    //    return DriverManager.getConnection(URL, USER, PASS);
    //}
}
