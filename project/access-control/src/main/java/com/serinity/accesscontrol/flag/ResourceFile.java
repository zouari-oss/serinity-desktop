// `ResourceFile` package name
package com.serinity.accesscontrol.flag;

/**
 * `access-control` filename manager (enum)
 *
 * <p>
 * Contain all the access-control package fxml filenames
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-28
 *
 *        <pre>{@code
 * // Example usage
 * final static String SIGNIN_FILENAME = ResourceFile.SIGNIN.getFileName();
 * }</pre>
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/ResourceFile.java">
 *        ResourceFile.java
 *        </a>
 */
public enum ResourceFile {
  LOGIN_FXML("/fxml/login.fxml"),
  DASHBOARD_FXML("/fxml/dashboard.fxml"),
  RESET_PASSWORD_FXML("/fxml/reset-password.fxml"),
  LOGIN_SIDE_HTML("/html/login-side.html"),
  FORGET_PASSWORD_HTML("/html/forgot-password.html");

  private final String fileName;

  private ResourceFile(final String fileName) {
    this.fileName = fileName;
  }

  public final String getFileName() {
    return fileName;
  }

  @Override
  public String toString() {
    return fileName;
  }
} // ResourceFile enum
