// `UserRole` package name
package com.serinity.accesscontrol.flag;

import com.serinity.accesscontrol.util.I18nUtil;

/**
 * Represents the different roles a user can have within the system
 *
 * <p>
 * This enum is used to define and enforce access control in the application.
 * Each role grants different permissions and access levels:
 * </p>
 * <ul>
 * <li>{@link ADMIN} - Full access to all system features and administrative
 * operations</li>
 * <li>{@link THERAPIST} - Access to patient data, appointments, and therapy
 * related features</li>
 * <li>{@link PATIENT} - Limited access to personal data, appointments, mood
 * tracking, and resources</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.User
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/UserRole.java">
 *      UserRole.java
 *      </a>
 */
public enum UserRole {
  ADMIN(""),
  THERAPIST("user.role.therapist"),
  PATIENT("user.role.patient");

  private final String key;

  private UserRole(final String key) {
    this.key = key;
  }

  public String getKey() {
    return key;
  }

  @Override
  public String toString() {
    return I18nUtil.getValue(key);
  }
} // UserRole enum
