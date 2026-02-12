// `SkinnedRatOrmEntityManager` package
package com.serinity.accesscontrol.config;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `java` import(s)
import java.sql.*;

public class SkinnedRatOrmEntityManager {
  public static Connection getConnection() {
    try {
      Connection connection = DriverManager.getConnection(
          EnvironmentVariableLoader.getDatabaseUrl(),
          EnvironmentVariableLoader.getDatabaseUsername(),
          EnvironmentVariableLoader.getDatabasePASSWORD());
      connection.setAutoCommit(true);
      return connection;
    } catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

  public static EntityManager getEntityManager() {
    try {
      Class.forName(EnvironmentVariableLoader.getJdbcDriver());
    } catch (final ClassNotFoundException e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }

    Connection connection = getConnection();
    return new EntityManager(connection);
  }
} // SkinnedRatOrmEntityManager class
