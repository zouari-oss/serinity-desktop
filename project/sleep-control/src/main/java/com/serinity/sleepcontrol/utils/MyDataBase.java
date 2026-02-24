package com.serinity.sleepcontrol.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class MyDataBase {

  private static MyDataBase instance;
  private Connection connection;

  private static final String URL = "jdbc:mariadb://127.0.0.1:3306/serinity_sleep?useSSL=false&serverTimezone=UTC&autoReconnect=true";
  private static final String USER = "root";
  private static final String PASS = "";

  private MyDataBase() {
    connect();
  }

  private void connect() {
    try {
      Class.forName("org.mariadb.jdbc.Driver");
      connection = DriverManager.getConnection(URL, USER, PASS);
      System.out.println("Connected to DB");
    } catch (ClassNotFoundException e) {
      System.err.println("MySQL Driver introuvable : " + e.getMessage());
    } catch (SQLException e) {
      System.err.println("Erreur connexion DB : " + e.getMessage());
    }
  }

  public static synchronized MyDataBase getInstance() {
    if (instance == null) {
      instance = new MyDataBase();
    }
    return instance;
  }

  public Connection getConnection() {
    try {
      if (connection == null || connection.isClosed() || !connection.isValid(2)) {
        System.out.println("Reconnexion a la base de donnees...");
        connect();
      }
    } catch (SQLException e) {
      System.err.println("Erreur verification connexion : " + e.getMessage());
      connect();
    }
    return connection;
  }
}
