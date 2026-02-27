// `SkinnedRatOrmEntityManager` package
package com.serinity.accesscontrol.config;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.core.EntityManager;

// `java` import(s)
import java.sql.*;

/**
 * Configuration utility class responsible for providing database connections
 * and initializing the SkinnedRat ORM {@link EntityManager}.
 *
 * <p>
 * This class centralizes the creation and configuration of JDBC connections
 * using environment variables loaded via {@link EnvironmentVariableLoader}.
 * It ensures that the appropriate JDBC driver is loaded before creating
 * the ORM {@link EntityManager}.
 * </p>
 *
 * <p>
 * Responsibilities:
 * </p>
 * <ul>
 * <li>Establishing a JDBC {@link java.sql.Connection}</li>
 * <li>Enabling auto-commit mode for database transactions</li>
 * <li>Loading the configured JDBC driver class</li>
 * <li>Providing a fully initialized {@link EntityManager}</li>
 * </ul>
 *
 * <p>
 * If a database connection cannot be established or the JDBC driver
 * cannot be found, the underlying exception is wrapped and rethrown
 * as a {@link RuntimeException}.
 * </p>
 *
 * <p>
 * This class uses static factory methods and does not require instantiation.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-16
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/config/SkinnedRatOrmEntityManager.java">
 *        SkinnedRatOrmEntityManager.java
 *        </a>
 */
public class SkinnedRatOrmEntityManager {
  private static final org.apache.logging.log4j.Logger _LOGGER = org.apache.logging.log4j.LogManager
      .getLogger(SkinnedRatOrmEntityManager.class);

  /**
   * Creates and returns a new JDBC {@link Connection} using credentials loaded
   * from environment variables, with auto-commit enabled.
   *
   * @return a new {@link Connection} ready for use
   * @throws RuntimeException if the connection cannot be established
   */
  public static Connection getConnection() {
    try {
      final Connection connection = DriverManager.getConnection(
          EnvironmentVariableLoader.getDatabaseUrl(),
          EnvironmentVariableLoader.getDatabaseUsername(),
          EnvironmentVariableLoader.getDatabasePassword());
      connection.setAutoCommit(true);
      return connection;
    } catch (final Exception e) {
      _LOGGER.error("Failed to establish database connection", e);
      throw new RuntimeException(e);
    }
  }

  /**
   * Loads the configured JDBC driver and returns an initialized
   * {@link EntityManager} backed by a fresh database connection.
   *
   * @return a fully configured {@link EntityManager}
   * @throws RuntimeException if the JDBC driver class is not found or the
   *                          connection cannot be established
   */
  public static EntityManager getEntityManager() {
    try {
      Class.forName(EnvironmentVariableLoader.getJdbcDriver());
    } catch (final ClassNotFoundException e) {
      _LOGGER.error("JDBC driver not found", e);
      throw new RuntimeException(e);
    }

    final Connection connection = getConnection();
    return new EntityManager(connection);
  }
} // SkinnedRatOrmEntityManager class
