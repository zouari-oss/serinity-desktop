package com.serinity.forumcontrol.Utils;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBaseTest {

    private static final String URL =
            "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
