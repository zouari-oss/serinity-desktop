/**
 * MessageKey.java
 *
 * Key-Value messages manager (enum)
 *
 * <p>see `/i18n/*`</p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-30
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/MessageKey.java" 
 * target="_blank">
 * MessageKey.java
 * </a>
 */

// `MessageKey` package name
package com.serinity.accesscontrol.flag;

/**
 * Key-Value messages manager
 *
 * <p>
 * none
 * </p>
 *
 * <pre>
 * {@code
 * // Example uasage
 * final static String EXIT_VALUE = MessageKey.EXIT.getValue();
 * }</pre>
 */
public enum MessageKey {
  GREETING("greeting"),
  LOGIN("login"),
  EXIT("exit"),
  LANGUAGES("languages");

  private final String value;

  MessageKey(String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }
} // MessageKey enum
