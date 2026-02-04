/**
 * Represents the different user presence status
 *
 * <p>This enum is used to define and enforce user presence status</p>
 *
 * <p>Status can be:</p>
 * <ul>
 *   <li>{@link ACTIVE}   - User allowed to log in</li>
 *   <li>{@link DISABLED} - User is banned / blocked</li>
 * </ul>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-03
 * @see     com.serinity.accesscontrol.model.User
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/PresenceStatus.java"
 * target="_blank">
 * PresenceStatus.java
 * </a>
 */

// `AccountStatus` package name
package com.serinity.accesscontrol.flag;

public enum AccountStatus {
  ACTIVE,
  DISABLED
} // AccountStatus enum
