// `AuditAction` package name
package com.serinity.accesscontrol.flag;

/**
 * Represents the different audit actions that can be recorded
 * in the system.
 *
 * <p>
 * This enum is used to define and enforce all possible audit
 * event types triggered by authentication, security, or user
 * management operations.
 * </p>
 *
 * <p>
 * Actions can include:
 * </p>
 * <ul>
 * <li>{@link #USER_SIGN_UP} - A new user account was created</li>
 * <li>{@link #USER_LOGIN} - A user successfully logged in</li>
 * <li>{@link #USER_LOGIN_FAILED} - A login attempt failed</li>
 * <li>{@link #USER_LOGOUT} - A user logged out</li>
 * <li>{@link #TOKEN_REFRESH} - A refresh token was used</li>
 * <li>{@link #SESSION_REVOKED} - A session was manually revoked</li>
 * <li>{@link #PASSWORD_CHANGED} - A user changed their password</li>
 * <li>{@link #USER_UPDATED} - A user account was updated</li>
 * <li>{@link #USER_DELETED} - A user account was deleted</li>
 * <li>{@link #ROLE_CHANGED} - A user role was modified</li>
 * </ul>
 *
 * <p>
 * This ensures that audit logging remains consistent,
 * type-safe, and refactor-proof across the system.
 * </p>
 *
 * @author @ZouariOmar
 * @version 1.0
 * @since 2026-02-17
 *
 * @see com.serinity.accesscontrol.model.AuditLog
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/AuditAction.java">
 *      AuditAction.java
 *      </a>
 */
public enum AuditAction {
  // Auth events
  USER_SIGN_UP("USER_SIGN_UP"),
  USER_LOGIN("USER_LOGIN"),
  USER_LOGOUT("USER_LOGOUT"),
  USER_LOGIN_FAILED("USER_LOGIN_FAILED"),
  TOKEN_REFRESH("TOKEN_REFRESH"),
  SESSION_REVOKED("SESSION_REVOKED"),

  // Security events
  PASSWORD_CHANGED("PASSWORD_CHANGED"),

  // User Management
  USER_UPDATED("USER_UPDATED"),
  USER_DELETED("USER_DELETED"),
  ROLE_CHANGED("ROLE_CHANGED");

  private final String value;

  AuditAction(final String value) {
    this.value = value;
  }

  public String getValue() {
    return value;
  }

  @Override
  public String toString() {
    return value;
  }
} // AuditAction enum
