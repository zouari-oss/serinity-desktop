// `EnvironmentVariableLoader` package name
package com.serinity.accesscontrol.config;

// `github` package(s)
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

/**
 * Utility class responsible for loading environment variables from a
 * configuration file using the Dotenv library.
 *
 * <p>
 * This class centralizes access to sensitive configuration values such as
 * database credentials and JDBC driver settings.
 * </p>
 *
 * <p>
 * The class uses a static initialization block to ensure that environment
 * variables are loaded once and remain accessible throughout the application
 * lifecycle.
 * </p>
 *
 * <p>
 * Exposed configuration values:
 * </p>
 * <ul>
 * <li>{@code DATABASE_URL} – Database connection URL</li>
 * <li>{@code DATABASE_USERNAME} – Database authentication username</li>
 * <li>{@code DATABASE_PASSWORD} – Database authentication password</li>
 * <li>{@code JDBC_DRIVER} – Fully qualified JDBC driver class name</li>
 * <li>more..</li>
 * </ul>
 *
 * <p>
 * If the environment file is missing or cannot be loaded, a
 * {@link io.github.cdimascio.dotenv.DotenvException} is thrown and wrapped
 * in a {@link RuntimeException}.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-16
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/config/EnvironmentVariableLoader.java">
 *        EnvironmentVariableLoader.java
 *        </a>
 */
public class EnvironmentVariableLoader {
  private final static Dotenv dotenv;

  static {
    try {
      dotenv = Dotenv.configure()
          .filename(".env")
          .ignoreIfMissing()
          .load();
    } catch (final DotenvException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  public final static String getDatabaseUrl() {
    return dotenv.get("DATABASE_URL");
  }

  public final static String getDatabaseUsername() {
    return dotenv.get("DATABASE_USERNAME");
  }

  public final static String getDatabasePassword() {
    return dotenv.get("DATABASE_PASSWORD");
  }

  public final static String getJdbcDriver() {
    return dotenv.get("JDBC_DRIVER");
  }

  public final static String getSmtpHost() {
    return dotenv.get("SMTP_HOST");
  }

  public final static int getSmtpPort() {
    return Integer.parseInt(dotenv.get("SMTP_PORT"));
  }

  public final static String getSmtpPassword() {
    return dotenv.get("SMTP_PASSWORD");
  }

  public final static String getSmtpUsername() {
    return dotenv.get("SMTP_USERNAME");
  }

  public final static String getSmtpFromName() {
    return dotenv.get("SMTP_FROM_NAME");
  }

  public final static boolean isSmptTls() {
    return dotenv.get("SMTP_USE_TLS").equals("true");
  }
} // EnvironmentVariableLoader class
