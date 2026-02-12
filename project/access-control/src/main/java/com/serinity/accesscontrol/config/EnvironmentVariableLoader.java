
// `EnvironmentVariableLoader` package name
package com.serinity.accesscontrol.config;

// `github` package(s)
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvException;

public class EnvironmentVariableLoader {
  private final static Dotenv dotenv;

  static {
    try {
      dotenv = Dotenv.configure()
          .filename(".env.development")
          .ignoreIfMissing()
          .load();
    } catch (final DotenvException e) {
      e.printStackTrace();
      throw new RuntimeException();
    }
  }

  public final static String getDatabaseUrl() {
    return dotenv.get("DATABASE_URL").toString();
  }

  public final static String getDatabaseUsername() {
    return dotenv.get("DATABASE_USERNAME").toString();
  }

  public final static String getDatabasePASSWORD() {
    return dotenv.get("DATABASE_PASSWORD").toString();
  }

  public final static String getJdbcDriver() {
    return dotenv.get("JDBC_DRIVER").toString();
  }
} // EnvironmentVariableLoader class
