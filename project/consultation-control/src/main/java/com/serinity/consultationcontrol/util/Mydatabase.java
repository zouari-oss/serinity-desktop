package com.serinity.consultationcontrol.util;

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
            Class.forName("org.mariadb.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);
            System.out.println("Connection established");
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException("Failed to initialize consultation DB connection", e);
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
