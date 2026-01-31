package com.serinity.moodcontrol.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import io.github.cdimascio.dotenv.Dotenv;

public final class DbConnection {
  private final static Dotenv dotenv = Dotenv.configure()
      .filename(".env.development")
      .ignoreIfMalformed()
      .ignoreIfMissing()
      .load();

  private DbConnection() {
  }

  private static final String URL = dotenv.get("DATABASE_URL"),
      USER = dotenv.get("DATABASE_USERNAME"),
      PASS = dotenv.get("DATABASE_PASSWORD");

  public static Connection getConnection() throws SQLException {
    System.out.println(dotenv.entries());
    return DriverManager.getConnection(URL, USER, PASS);
  }
} // DbConnection class
