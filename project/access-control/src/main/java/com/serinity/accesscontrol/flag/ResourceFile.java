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
  /** Root layout FXML. */
  ROOT_FXML("/fxml/root.fxml"),
  /** Login page FXML. */
  LOGIN_FXML("/fxml/login.fxml"),
  /** User home page FXML. */
  USER_HOME_FXML("/fxml/user-home.fxml"),
  /** User dashboard FXML. */
  USER_DAHBOARD_FXML("/fxml/user-dashboard.fxml"),
  /** Admin dashboard FXML. */
  ADMIN_DASHBOARD_FXML("/fxml/admin-dashboard.fxml"),
  /** Admin user management FXML. */
  ADMIN_USERS_MANAGMENT_FXML("/fxml/admin-users-management.fxml"),
  /** Reset password FXML. */
  RESET_PASSWORD_FXML("/fxml/reset-password.fxml"),
  /** Camera desktop FXML. */
  CAMERA_DESKTOP_FXML("/fxml/camera-desktop.fxml"),
  /** Mood home FXML. */
  MOOD_HOME_FXML("/fxml/mood/MoodHome.fxml"),
  /** Admin mood management FXML. */
  MOOD_ADMIN_MANAGMENT("/fxml/backoffice/admin-mood-managment.fxml"),
  /** Forum home FXML. */
  FORUM_HOME_FXML("/fxml/ForumPostsView.fxml"),
  /** Admin forum FXML. */
  FORUM_ADMIN_FXML("/fxml/ForumBackoffice.fxml"),

  /** Login side HTML page. */
  LOGIN_SIDE_HTML("/html/login-side.html"),
  /** Forgot-password HTML page. */
  FORGET_PASSWORD_HTML("/html/forgot-password.html"),

  /** SCRFD detection model. */
  ANTELOPEV2_SCRFD_10G_BNKPS_ONNX("antelopev2/scrfd_10g_bnkps.onnx"),
  /** ArcFace recognition model. */
  ANTELOPEV2_GLINTR100_ONNX("antelopev2/glintr100.onnx"),

  /** Default user profile image. */
  USER_DEFAUL_PROFILE_PNG("/assets/user-dashboard/user-default-profile.png");

  private final String fileName;

  /**
   * Creates a resource-file enum entry.
   *
   * @param fileName classpath resource path
   */
  private ResourceFile(final String fileName) {
    this.fileName = fileName;
  }

  /**
   * Returns the resource path.
   *
   * @return classpath resource path
   */
  public final String getFileName() {
    return fileName;
  }

  @Override
  public String toString() {
    return fileName;
  }
} // ResourceFile enum
