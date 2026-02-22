// `AccountStatus` package name
package com.serinity.accesscontrol.flag;

// `serinity` import(s)
import com.serinity.accesscontrol.util.I18nUtil;

/**
 * Represents the different user presence status
 *
 * <p>
 * This enum is used to define and enforce user presence status
 * </p>
 *
 * <p>
 * Status can be:
 * </p>
 * <ul>
 * <li>{@link ACTIVE} - User allowed to log in</li>
 * <li>{@link DISABLED} - User is banned / blocked</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.User
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/PresenceStatus.java">
 *      PresenceStatus.java
 *      </a>
 */
public enum AccountStatus {
  ACTIVE("user.status.active"),
  DISABLED("user.status.disabled");

  private final String key;

  private AccountStatus(final String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  @Override
  public String toString() {
    return I18nUtil.getValue(key);
  }
} // AccountStatus enum
