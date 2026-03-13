package com.serinity.accesscontrol.util.consultation;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
public class Mydatabase {
    private static Mydatabase instance;
    private final String URL = "jdbc:mariadb://localhost:3306/serinity";
    private final String USER = "root";
    private final String PASSWORD = "root";
    private  Connection connection;
    public Mydatabase(){
        try {
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connection established");
        } catch (SQLException e) {
            // throw new RuntimeException(e);
            System.err.println(e.getMessage());
        }
    }
    public static Mydatabase getInstance(){
        if(instance==null)
            instance = new Mydatabase();
        return instance;
    }
    public Connection getConnection() {
        return connection;
    }
}
