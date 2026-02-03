/**
 * AuditLog.java
 *
 * Represents an audit log entry in the system. Used to record actions performed by users,
 * along with metadata such as the IP address and timestamp of the action.
 *
 * <p>This entity extends {@link IdentifiableEntity}, which provides a unique {@code id} for
 * each log entry.</p>
 *
 * <p>The table {@code audit_logs} has the following indexes for performance optimization:</p>
 * <ul>
 *   <li>{@code idx_audit_user}    - Indexed on {@code user_id} for quick lookup by user.</li>
 *   <li>{@code idx_audit_created} - Indexed on {@code created_at} to speed up time-based queries.</li>
 * </ul>
 *
 * <p>Fields include:</p>
 * <ul>
 *   <li>{@code action}    - A description of the action performed.</li>
 *   <li>{@code ipAddress} - IP address of the user who performed the action.</li>
 *   <li>{@code createdAt} - Timestamp when the action was performed.</li>
 *   <li>{@code user}      - The {@link com.serinity.accesscontrol.model.User} who performed the action.</li>
 * </ul>
 *
 * @author  @ZouariOmar <zouariomar20@gmail.com>
 * @version 1.0
 * @since   2026-02-03
 * @see     com.serinity.accesscontrol.model.User
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/AuditLog.java"
 * target="_blank">
 * AuditLog.java
 * </a>
 */

// `AuditLog` package name
package com.serinity.accesscontrol.model;

// `jakarta` import(s)
import jakarta.persistence.*;
import java.time.Instant;

// `serinity` import(s)
import com.serinity.accesscontrol.model.base.IdentifiableEntity;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_audit_user", columnList = "user_id"),
    @Index(name = "idx_audit_created", columnList = "created_at")
})
public final class AuditLog extends IdentifiableEntity {
  @Column(nullable = false, length = 100)
  private String action;

  @Column(name = "ip_address", nullable = false, length = 45)
  private String ipAddress;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @ManyToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, updatable = false)
  private User user;

  @PrePersist
  protected void onCreate() {
    this.createdAt = Instant.now();
  }
} // AuditLog final class
