/**
 * FXMLFile.java
 *
 * Fxml filename manager (enum)
 *
 * <p>see `/fxml/*`</p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-28
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/FXMLFile.java" 
 * target="_blank">
 * FXMLFile.java
 * </a>
 */

// `FXMLFile` package name
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
 * final static String SIGNIN_FILENAME = FXMLFile.SIGNIN.getFileName();
 * }</pre>
 */
public enum FXMLFile {
  SIGNIN("/fxml/sign-in.fxml");

  private final String fileName;

  private FXMLFile(final String fileName) {
    this.fileName = fileName;
  }

  public final String getFileName() {
    return fileName;
  }
} // FXMLFile enum
