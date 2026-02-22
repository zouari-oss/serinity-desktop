// `User` package name
package com.serinity.accesscontrol.model;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.annotations.Column;
import org.zouarioss.skinnedratorm.annotations.Entity;
import org.zouarioss.skinnedratorm.annotations.Enumerated;
import org.zouarioss.skinnedratorm.annotations.OneToOne;
import org.zouarioss.skinnedratorm.annotations.PrePersist;
import org.zouarioss.skinnedratorm.annotations.Table;
import org.zouarioss.skinnedratorm.flag.CascadeType;
import org.zouarioss.skinnedratorm.flag.EnumType;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.AccountStatus;
import com.serinity.accesscontrol.flag.PresenceStatus;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.base.TimestampedEntity;

/**
 * Represents a system user within the Access Control application.
 *
 * <p>
 * This entity is mapped to the {@code users} database table
 * </p>
 *
 * <p>
 * Note: This class is declared {@code final} to prevent inheritance and ensure
 * session integrity.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-02
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/User.java">
 *        User.java
 *        </a>
 */
@Entity
@Table(name = "users")
public final class User extends TimestampedEntity {
  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(name = "presence_status", nullable = false)
  private PresenceStatus presenceStatus; // Pre-persist

  @Enumerated(EnumType.STRING)
  @Column(name = "account_status", nullable = false)
  private AccountStatus accountStatus; // Pre-persist

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false) // User must have a profile
  private Profile profile;

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getPassword() {
    return password;
  }

  public void setPasswordHash(final String passwordHash) {
    this.password = passwordHash;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(final UserRole role) {
    this.role = role;
  }

  public PresenceStatus getPresenceStatus() {
    return presenceStatus;
  }

  public void setPresenceStatus(final PresenceStatus presenceStatus) {
    this.presenceStatus = presenceStatus;
  }

  public AccountStatus getAccountStatus() {
    return accountStatus;
  }

  public void setAccountStatus(final AccountStatus accountStatus) {
    this.accountStatus = accountStatus;
  }

  // #############################
  // ### PRE_PERSIST METHOD(S) ###
  // #############################

  @PrePersist
  protected void onAction() {
    // Set accountStatus if not already set
    if (this.accountStatus == null) {
      this.accountStatus = AccountStatus.ACTIVE;
    }

    // Set presenceStatus if not already set
    if (this.presenceStatus == null)
      this.presenceStatus = PresenceStatus.ONLINE;
  }
} // User final class
