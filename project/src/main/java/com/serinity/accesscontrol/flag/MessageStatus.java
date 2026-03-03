// `MessageStatus` package name
package com.serinity.accesscontrol.flag;

/**
 * Message Status enum
 *
 * <p>
 * Represents a message status type with an associated CSS class and optional
 * icon path.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-30
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/MessageStatus.java">
 *        MessageStatus.java
 *        </a>
 */
public enum MessageStatus {
  SUCCESS("success", "/assets/root/icons8-success-50.png"),
  ERROR("error", "/assets/root/icons8-error-50.png"),
  WARNING("warning", "/assets/root/icons8-warning-50.png"),
  INFO("info", "/assets/root/icons8-info-50.png");

  private final String cssClass;
  private final String iconPath;

  MessageStatus(final String cssClass, final String iconPath) {
    this.cssClass = cssClass;
    this.iconPath = iconPath;
  }

  /**
   * Returns the CSS class name associated with this status.
   *
   * @return CSS class name
   */
  public String getCssClass() {
    return cssClass;
  }

  /**
   * Returns the icon path associated with this status.
   *
   * @return icon path
   */
  public String getIconPath() {
    return iconPath;
  }
} // MessageStatus enum
