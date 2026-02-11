/**
 * ResourceFile.java
 *
 * `access-control` filename manager (enum)
 *
 * <p>Manage `.fxml, .cfg, .xml.cfg, ..` files</p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-28
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/ResourceFile.java" 
 * target="_blank">
 * ResourceFile.java
 * </a>
 */

// `ResourceFile` package name
package com.serinity.accesscontrol.flag;

/**
 * Fxml file manager (enum)
 *
 * <p>
 * Contain all the access-control package fxml filenames
 * </p>
 *
 * <pre>
 * {@code
 * // Example usage
 * final static String SIGNIN_FILENAME = ResourceFile.SIGNIN.getFileName();
 * }</pre>
 */
public enum ResourceFile {
  LOGIN_FXML("/fxml/login.fxml"),
  LOGIN_SIDE_HTML("/html/login-side.html"),
  HIBERNATE_CFG_FXML("hibernate.cfg.xml");

  private final String fileName;

  private ResourceFile(final String fileName) {
    this.fileName = fileName;
  }

  public final String getFileName() {
    return fileName;
  }
} // ResourceFile enum
