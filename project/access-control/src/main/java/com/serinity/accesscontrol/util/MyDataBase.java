package com.serinity.accesscontrol.util;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {
    private static MyDataBase instance ;
    final String URL = "jdbc:mariadb://127.0.0.1:3306/serinity";
    final String USERNAME = "root";
    final String PASSWORD = "";
    private Connection cnx ;

    private MyDataBase() {
        try {
            Class.forName("org.mariadb.jdbc.Driver"); // add this
            cnx = DriverManager.getConnection(URL, USERNAME, PASSWORD);
            System.out.println("Connected ....");
        } catch (ClassNotFoundException e) {
            System.out.println("Driver not found: " + e.getMessage());
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    public static MyDataBase getInstance(){
        if(instance ==null)
            instance = new MyDataBase();
        return instance ;
    }

    public Connection getCnx() {
        return cnx;
    }
}

