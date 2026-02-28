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
  // FXML FILE(s)
  ROOT_FXML("/fxml/root.fxml"),
  LOGIN_FXML("/fxml/login.fxml"),
  USER_HOME_FXML("/fxml/user-home.fxml"),
  USER_DAHBOARD_FXML("/fxml/user-dashboard.fxml"),
  ADMIN_DASHBOARD_FXML("/fxml/admin-dashboard.fxml"),
  ADMIN_USERS_MANAGMENT_FXML("/fxml/admin-users-management.fxml"),
  RESET_PASSWORD_FXML("/fxml/reset-password.fxml"),
  CAMERA_DESKTOP_FXML("/fxml/camera-desktop.fxml"),

  // HTML FILE(s)
  LOGIN_SIDE_HTML("/html/login-side.html"),
  FORGET_PASSWORD_HTML("/html/forgot-password.html"),

  // ONNX FILE(s)
  ANTELOPEV2_SCRFD_10G_BNKPS_ONNX("antelopev2/scrfd_10g_bnkps.onnx"),
  ANTELOPEV2_GLINTR100_ONNX("antelopev2/glintr100.onnx"),

  // IMAGE(s)
  USER_DEFAUL_PROFILE_PNG("/assets/user-dashboard/user-default-profile.png");

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
