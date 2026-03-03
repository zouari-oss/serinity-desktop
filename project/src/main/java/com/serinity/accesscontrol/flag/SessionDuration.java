// `SessionDuration` package name
package com.serinity.accesscontrol.flag;

/**
 * Represents predefined durations for sessions, tokens, or other timed events.
 *
 * <p>
 * Each enum constant defines a specific duration that can be used to calculate
 * expiration times in the system.
 * </p>
 *
 * <ul>
 * <li>{@link #ADMIN_SESSION} - Admin sessions expire after 1 day</li>
 * <li>{@link #USER_SESSION} - Therapist/Patient sessions expire after 7
 * days</li>
 * </ul>
 *
 * <pre>{@code
 * Instant expiresAt = SessionDuration.PATIENT_SESSION.fromNow();
 * }</pre>
 *
 * @author @ZouariOmar
 * @version 1.0
 * @since 2026-02-17
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/flag/SessionDuration.java">
 *        SessionDuration.java
 *        </a>
 */
public enum SessionDuration {
  ADMIN_SESSION(1),
  USER_SESSION(7);

  private final long duration;

  SessionDuration(final long duration) {
    this.duration = duration;
  }

  public long getDuration() {
    return duration;
  }

  @Override
  public String toString() {
    return Long.toString(duration);
  }
} // SessionDuration enum
