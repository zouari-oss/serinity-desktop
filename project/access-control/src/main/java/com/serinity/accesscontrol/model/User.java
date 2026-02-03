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
import com.serinity.accesscontrol.flag.Role;
import com.serinity.accesscontrol.model.base.TimestampedEntity;

// `jakarta` import(s)
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
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
  private Role role;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false) // User must have a profile
  private Profile profile;

  @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
  private java.util.List<AuthSession> sessions = new java.util.ArrayList<>();

  @OneToMany(mappedBy = "user", cascade = CascadeType.PERSIST)
  private java.util.List<AuditLog> auditLogs = new java.util.ArrayList<>();

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################

  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  public String getPasswordHash() {
    return passwordHash;
  }

  public void setPasswordHash(final String passwordHash) {
    this.passwordHash = passwordHash;
  }

  public Role getRole() {
    return role;
  }

  public void setRole(final Role role) {
    this.role = role;
  }

  public Profile getProfile() {
    return profile;
  }

  public void setProfile(final Profile profile) {
    this.profile = profile;
  }

  public java.util.List<AuthSession> getSessions() {
    return sessions;
  }

  public void setSessions(final java.util.List<AuthSession> sessions) {
    this.sessions = sessions;
  }

  public java.util.List<AuditLog> getAuditLogs() {
    return auditLogs;
  }

  public void setAuditLogs(final java.util.List<AuditLog> auditLogs) {
    this.auditLogs = auditLogs;
  }
} // User final class
