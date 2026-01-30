/**
 * SystemInfo.java
 *
 * `access-control` module system information
 *
 * <p>none</p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-28
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/SystemInfo.java"
 * target="_blank">
 * SystemInfo.java
 * </a>
 */

// `SystemInfo` package name
package com.serinity.accesscontrol;

/**
 * `access-control` module system information
 *
 * <p>
 * none
 * </p>
 *
 * <pre>
 * {@code
 * finl static String JAVAFX_VERSION = SystemInfo.javafxVersion();
 * }</pre>
 */
public class SystemInfo {
  public static String javaVersion() {
    return System.getProperty("java.version");
  }

  public static String javafxVersion() {
    return System.getProperty("javafx.version");
  }
} // SystemInfo class
