/**
 * User.java
 *
 * <p>
 * Represents a system user within the Access Control application.
 * This entity is mapped to the {@code users} database table
 * </p>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-02
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/User.java"
 * target="_blank">
 * User.java
 * </a>
 */

// `User` package name
package com.serinity.accesscontrol.model;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.AccountStatus;
import com.serinity.accesscontrol.flag.UserRole;
import com.serinity.accesscontrol.model.base.TimestampedEntity;

// `jakarta` import(s)
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public final class User extends TimestampedEntity {
  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(name = "password", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private UserRole role;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private AccountStatus accountStatus = AccountStatus.ACTIVE; // Pre-persist

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false) // User must have a profile
  private Profile profile;

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public UserRole getRole() {
    return role;
  }

  public void setRole(UserRole role) {
    this.role = role;
  }

  public AccountStatus getAccountStatus() {
    return accountStatus;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(Profile profile) {
    this.profile = profile;
  }

  // #############################
  // ### PRE_PERSIST METHOD(S) ###
  // #############################

  @PrePersist
  protected void onAction() {
    if (this.accountStatus == null) {
      this.accountStatus = AccountStatus.ACTIVE;
    }
  }
} // User final class
