// `AuthSession` package name
package com.serinity.accesscontrol.model;

// `java` import(s)
import java.time.Duration;
import java.time.Instant;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.annotations.Column;
import org.zouarioss.skinnedratorm.annotations.CreationTimestamp;
import org.zouarioss.skinnedratorm.annotations.Entity;
import org.zouarioss.skinnedratorm.annotations.Index;
import org.zouarioss.skinnedratorm.annotations.JoinColumn;
import org.zouarioss.skinnedratorm.annotations.ManyToOne;
import org.zouarioss.skinnedratorm.annotations.PrePersist;
import org.zouarioss.skinnedratorm.annotations.Table;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.SessionDuration;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.base.IdentifiableEntity;
import com.serinity.accesscontrol.util.RefreshTokenGenerator;

/**
 * Represents an authentication session for a user. Used to manage user login
 * sessions, refresh tokens, and session validity.
 *
 * <p>
 * This entity extends {@link IdentifiableEntity}, which provides a unique
 * {@code id} for
 * each session entry.
 * </p>
 *
 * <p>
 * The table {@code auth_sessions} has the following indexes for performance
 * optimization:
 * </p>
 * <ul>
 * <li>{@code idx_session_token} - Indexed on {@code refresh_token} to quickly
 * validate or revoke sessions.</li>
 * <li>{@code idx_session_user} - Indexed on {@code user_id} for efficient
 * lookup of all sessions by user.</li>
 * </ul>
 *
 * <p>
 * Fields include:
 * </p>
 * <ul>
 * <li>{@code refreshToken} - The refresh token associated with this session.
 * Must be unique.</li>
 * <li>{@code createdAt} - Timestamp when the session was created.</li>
 * <li>{@code expiresAt} - Timestamp when the session expires and is no longer
 * valid.</li>
 * <li>{@code revoked} - Boolean flag indicating if the session has been
 * revoked.</li>
 * <li>{@code user} - The {@link com.serinity.accesscontrol.model.User} who owns
 * this session.</li>
 * </ul>
 *
 * <p>
 * Note: This class is declared {@code final} to prevent inheritance and ensure
 * session integrity.
 * </p>
 *
 * @author @ZouariOmar <zouariomar20@gmail.com>
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.User
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/AuthSession.java">
 *      AuthSession.java
 *      </a>
 */
@Entity
@Table(name = "auth_sessions")
@Index(name = "idx_session_token", columnList = "refresh_token")
@Index(name = "idx_session_user", columnList = "user_id")
public final class AuthSession extends IdentifiableEntity {
  @Column(name = "refresh_token", nullable = false, unique = true, length = 255)
  private String refreshToken; // Pre-persist - From `RefreshTokenGenerator`

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  // In MySQL before 5.6, TIMESTAMP columns cannot have NULL or non-current
  // defaults if NOT NULL is set.
  @Column(name = "expires_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP DEFAULT current_timestamp()")
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean revoked = false;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################
  public String getRefreshToken() {
    return refreshToken;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public void setCreatedAt(final Instant createdAt) {
    this.createdAt = createdAt;
  }

  public Instant getExpiresAt() {
    return expiresAt;
  }

  public void setExpiresAt(final Instant expiresAt) {
    this.expiresAt = expiresAt;
  }

  public void setRevoked(final boolean revoked) {
    this.revoked = revoked;
  }

  public boolean isRevoked() {
    return revoked;
  }

  public User getUser() {
    return user;
  }

  public void setUser(final User user) {
    this.user = user;
  }

  // #############################
  // ### PRE_PERSIST METHOD(S) ###
  // #############################
  @PrePersist
  private void onCreate() {
    if (this.expiresAt == null) {
      this.expiresAt = Instant.now().plus(Duration.ofDays(
          getUser().getRole().equals(UserRole.ADMIN)
              ? SessionDuration.ADMIN_SESSION.getDuration()
              : SessionDuration.USER_SESSION.getDuration()));
    }

    if (this.refreshToken == null)
      this.refreshToken = RefreshTokenGenerator.generate();
  }
} // AuthSession final class
