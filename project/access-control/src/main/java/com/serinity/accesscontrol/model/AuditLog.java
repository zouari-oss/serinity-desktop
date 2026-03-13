// `AuditLog` package name
package com.serinity.accesscontrol.model;

/// `java` import(s)
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
import org.zouarioss.skinnedratorm.flag.FetchType;

// `serinity` import(s)
import com.serinity.accesscontrol.model.base.IdentifiableEntity;
import com.serinity.accesscontrol.util.SystemInfo;

/**
 * Represents an audit log entry in the system. Used to record actions performed
 * by users,
 * along with metadata such as the IP address and timestamp of the action.
 *
 * <p>
 * This entity extends {@link IdentifiableEntity}, which provides a unique
 * {@code id} for
 * each log entry.
 * </p>
 *
 * <p>
 * The table {@code audit_logs} has the following indexes for performance
 * optimization:
 * </p>
 * <ul>
 * <li>{@code idx_audit_created} - Indexed on {@code created_at} to speed up
 * time-based queries.</li>
 * </ul>
 *
 * <p>
 * Fields include:
 * </p>
 * <ul>
 * <li>{@code action} - A description of the action performed.</li>
 * <li>{@code ipAddress} - IP address of the user who performed the action.</li>
 * <li>{@code createdAt} - Timestamp when the action was performed.</li>
 * <li>{@code user} - The {@link com.serinity.accesscontrol.model.User} who
 * performed the action.</li>
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
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/AuditLog.java">
 *      AuditLog.java
 *      </a>
 */
@Entity
@Table(name = "audit_logs")
@Index(name = "idx_audit_created", columnList = "created_at")
public final class AuditLog extends IdentifiableEntity {
  @Column(nullable = false, length = 100)
  private String action;

  @Column(name = "os_name", length = 50, nullable = true)
  private String osName; // Pre-persist - From system property

  @Column(length = 100, nullable = true)
  private String hostname; // Pre-persist - From system property

  @Column(name = "private_ip_address", nullable = false, length = 45)
  private String privateIpAddress; // Pre-persist - The current subnet private ip address

  @Column(name = "mac_address", length = 17, nullable = true)
  private String macAddress; // Pre-persist - From any network interface

  @Column(length = 255, nullable = true)
  private String location; // Pre-persist - From system property

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "auth_session_id", nullable = false)
  private AuthSession session;

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################

  public String getAction() {
    return action;
  }

  public void setAction(final String action) {
    this.action = action;
  }

  public String getOsName() {
    return osName;
  }

  public String getHostname() {
    return hostname;
  }

  public String getPrivateIpAddress() {
    return privateIpAddress;
  }

  public String getMacAddress() {
    return macAddress;
  }

  public String getLocation() {
    return location;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }

  public AuthSession getSession() {
    return session;
  }

  public void setSession(final AuthSession session) {
    this.session = session;
  }

  // #############################
  // ### PRE_PERSIST METHOD(S) ###
  // #############################

  @PrePersist
  private void onCreate() {
    if (this.hostname == null) {
      try {
        this.hostname = java.net.InetAddress.getLocalHost().getHostName();
      } catch (final Exception e) {
        this.hostname = "unknown";
      }
    }

    // Set ip address if not already set
    if (this.privateIpAddress == null) {
      this.privateIpAddress = SystemInfo.getPrivateIpAddress();
    }

    // Set MAC address if not already set (from any network interface)
    if (this.macAddress == null) {
      this.macAddress = SystemInfo.getMacAddress();
    }

    // Set OS name if not already set (from system property)
    if (this.osName == null) {
      this.osName = System.getProperty("os.name", "unknown");
    }

    // Set location if not already set (from system property)
    if (this.location == null) {
      // You can replace this with geolocation logic if available
      this.location = System.getProperty("user.country", "unknown");
    }
  }
} // AuditLog final class
