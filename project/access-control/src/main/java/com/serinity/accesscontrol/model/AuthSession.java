/**
 * AuthSession.java
 *
 * Represents an authentication session for a user. Used to manage user login sessions,
 * refresh tokens, and session validity.
 *
 * <p>This entity extends {@link IdentifiableEntity}, which provides a unique {@code id} for
 * each session entry.</p>
 *
 * <p>The table {@code auth_sessions} has the following indexes for performance optimization:</p>
 * <ul>
 *   <li>{@code idx_session_token} - Indexed on {@code refresh_token} to quickly validate or revoke sessions.</li>
 *   <li>{@code idx_session_user}  - Indexed on {@code user_id} for efficient lookup of all sessions by user.</li>
 * </ul>
 *
 * <p>Fields include:</p>
 * <ul>
 *   <li>{@code refreshToken} - The refresh token associated with this session. Must be unique.</li>
 *   <li>{@code createdAt}    - Timestamp when the session was created.</li>
 *   <li>{@code expiresAt}    - Timestamp when the session expires and is no longer valid.</li>
 *   <li>{@code revoked}      - Boolean flag indicating if the session has been revoked.</li>
 *   <li>{@code user}         - The {@link com.serinity.accesscontrol.model.User} who owns this session.</li>
 * </ul>
 *
 * <p>Note: This class is declared {@code final} to prevent inheritance and ensure session integrity.</p>
 *
 * @author  @ZouariOmar <zouariomar20@gmail.com>
 * @version 1.0
 * @since   2026-02-03
 * @see     com.serinity.accesscontrol.model.User
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/AuthSession.java"
 * target="_blank">
 * AuthSession.java
 * </a>
 */

// `AuthSession` package name
package com.serinity.accesscontrol.model;

// `jakarta` import(s)
import jakarta.persistence.*;
import java.time.Instant;

// `serinity` import(s)
import com.serinity.accesscontrol.model.base.IdentifiableEntity;

@Entity
@Table(name = "auth_sessions", indexes = {
    @Index(name = "idx_session_token", columnList = "refresh_token"),
    @Index(name = "idx_session_user", columnList = "user_id")
})
public final class AuthSession extends IdentifiableEntity {
  @Column(name = "refresh_token", nullable = false, unique = true, length = 255)
  private String refreshToken;

  @Column(nullable = false, updatable = false)
  private Instant createdAt;

  @Column(nullable = false, updatable = false)
  private Instant expiresAt;

  @Column(nullable = false)
  private boolean revoked = false;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now();
  }
} // AuthSession final class
