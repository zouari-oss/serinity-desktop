/**
 * Represents the different user presence status
 *
 * <p>This enum is used to define and enforce user presence status</p>
 *
 * <p>Status can be:</p>
 * <ul>
 *   <li>{@link ONLINE}  - User is loged in and already using the app</li>
 *   <li>{@link OFFLINE} - User is loged out</li>
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

// `PresenceStatus` package name
package com.serinity.accesscontrol.flag;

public enum PresenceStatus {
  ONLINE,
  OFFLINE
} // PresenceStatus enum
