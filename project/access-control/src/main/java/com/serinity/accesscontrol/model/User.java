// `User` package name
package com.serinity.accesscontrol.model;

import java.time.LocalDateTime;

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
  /** Creates an empty user entity. */
  public User() {
  }

  @Column(nullable = false, unique = true, length = 150)
  private String email;

  @Column(name = "google_id", unique = true, length = 191, nullable = true)
  private String googleId;

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

  @Column(name = "face_recognition_enabled", nullable = false)
  private Integer faceRecognitionEnabled; // tinyint(4), pre-persist

  @Column(name = "totp_secret_encrypted", length = 255, nullable = true)
  private String totpSecretEncrypted;

  @Column(name = "is_two_factor_enabled", nullable = false)
  private Integer isTwoFactorEnabled; // tinyint(4), pre-persist

  @Column(name = "totp_enabled_at", nullable = true)
  private LocalDateTime totpEnabledAt;

  @Column(name = "risk_level", length = 20, nullable = true)
  private String riskLevel;

  @OneToOne(mappedBy = "user", cascade = CascadeType.ALL, optional = false) // User must have a profile
  private Profile profile;

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################

  /**
   * Returns the user email address.
   *
   * @return user email
   */
  public String getEmail() {
    return email;
  }

  public void setEmail(final String email) {
    this.email = email;
  }

  /**
   * Returns the linked Google account identifier.
   *
   * @return Google account id
   */
  public String getGoogleId() {
    return googleId;
  }

  public void setGoogleId(final String googleId) {
    this.googleId = googleId;
  }

  /**
   * Returns the hashed password.
   *
   * @return password hash
   */
  public String getPassword() {
    return password;
  }

  public void setPasswordHash(final String passwordHash) {
    this.password = passwordHash;
  }

  /**
   * Returns the role assigned to this user.
   *
   * @return user role
   */
  public UserRole getRole() {
    return role;
  }

  public void setRole(final UserRole role) {
    this.role = role;
  }

  /**
   * Returns the current presence status.
   *
   * @return presence status
   */
  public PresenceStatus getPresenceStatus() {
    return presenceStatus;
  }

  public void setPresenceStatus(final PresenceStatus presenceStatus) {
    this.presenceStatus = presenceStatus;
  }

  /**
   * Returns the account status.
   *
   * @return account status
   */
  public AccountStatus getAccountStatus() {
    return accountStatus;
  }

  public void setAccountStatus(final AccountStatus accountStatus) {
    this.accountStatus = accountStatus;
  }

  /**
   * Indicates whether face recognition login is enabled.
   *
   * @return {@code true} if face recognition is enabled
   */
  public boolean isFaceRecognitionEnabled() {
    return faceRecognitionEnabled != null && faceRecognitionEnabled == 1;
  }

  public void setFaceRecognitionEnabled(final boolean faceRecognitionEnabled) {
    this.faceRecognitionEnabled = faceRecognitionEnabled ? 1 : 0;
  }

  /**
   * Returns the encrypted TOTP secret.
   *
   * @return encrypted TOTP secret
   */
  public String getTotpSecretEncrypted() {
    return totpSecretEncrypted;
  }

  public void setTotpSecretEncrypted(final String totpSecretEncrypted) {
    this.totpSecretEncrypted = totpSecretEncrypted;
  }

  /**
   * Indicates whether two-factor authentication is enabled.
   *
   * @return {@code true} if 2FA is enabled
   */
  public boolean isTwoFactorEnabled() {
    return isTwoFactorEnabled != null && isTwoFactorEnabled == 1;
  }

  public void setTwoFactorEnabled(final boolean isTwoFactorEnabled) {
    this.isTwoFactorEnabled = isTwoFactorEnabled ? 1 : 0;
  }

  /**
   * Returns the timestamp when TOTP was enabled.
   *
   * @return TOTP enabled timestamp
   */
  public LocalDateTime getTotpEnabledAt() {
    return totpEnabledAt;
  }

  public void setTotpEnabledAt(final LocalDateTime totpEnabledAt) {
    this.totpEnabledAt = totpEnabledAt;
  }

  /**
   * Returns the user's risk level.
   *
   * @return risk level
   */
  public String getRiskLevel() {
    return riskLevel;
  }

  public void setRiskLevel(final String riskLevel) {
    this.riskLevel = riskLevel;
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

    if (this.faceRecognitionEnabled == null) {
      this.faceRecognitionEnabled = 0;
    }

    if (this.isTwoFactorEnabled == null) {
      this.isTwoFactorEnabled = 0;
    }
  }
} // User final class
