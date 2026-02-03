/**
 * Profile.java
 *
 * Represents a user's profile in the system. Contains personal information, contact details,
 * and metadata related to the user's account. Each {@link Profile} is associated with exactly
 * one {@link com.serinity.accesscontrol.model.User}.
 *
 * <p>The {@code Profile} class extends {@link com.serinity.accesscontrol.model.TimestampedEntity},
 * which automatically provides {@code createdAt} and {@code updatedAt} timestamps.
 * Fields include:</p>
 * <ul>
 *   <li>{@code firstName}       - The user's first name (optional).</li>
 *   <li>{@code lastName}        - The user's last name (optional).</li>
 *   <li>{@code phone}           - The user's phone number (optional).</li>
 *   <li>{@code country}         - The user's country (optional).</li>
 *   <li>{@code state}           - The user's state/region (optional).</li>
 *   <li>{@code aboutMe}         - A short biography or description (optional).</li>
 *   <li>{@code username}        - Auto-generated unique username, immutable after creation.</li>
 *   <li>{@code profileImageUrl} - Optional URL to the user's profile image.</li>
 *   <li>{@code user}            - The {@link com.serinity.accesscontrol.model.User} this profile belongs to.</li>
 * </ul>
 *
 * <p><b>Note:</b> The {@code username} field is generated automatically when the profile is
 * created and cannot be modified manually. This ensures uniqueness across the system.</p>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-03
 * @see     com.serinity.accesscontrol.model.User
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/Profile.java"
 * target="_blank">
 * Profile.java
 * </a>
 */

// `User` package name
package com.serinity.accesscontrol.model;

// `serinity` import(s)
import com.serinity.accesscontrol.model.base.TimestampedEntity;
import com.serinity.accesscontrol.util.UsernameGenerator;

// `jakarta` import(s)
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "profiles", uniqueConstraints = {
    @UniqueConstraint(name = "uk_profile_username", columnNames = "username")
})
public final class Profile extends TimestampedEntity {
  @Column(nullable = false, length = 50, updatable = false)
  private String username;

  @Column(nullable = true)
  private String firstName;

  @Column(nullable = true)
  private String lastName;

  @Column(length = 20, nullable = true)
  private String phone;

  @Column(name = "profile_image_url", length = 512, nullable = true)
  private String profileImageUrl;

  @Column(length = 100, nullable = true)
  private String country; // e.g., Tunisia, Marroc, ..

  @Column(length = 100, nullable = true)
  private String state; // e.g., Sfax, Tunis, ..

  @Column(length = 500, nullable = true)
  private String aboutMe;

  @OneToOne
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  /**
   * Auto-generate username ONCE.
   * Immutable after creation.
   */
  @PrePersist
  protected void generateUsername() {
    if (this.username == null) {
      this.username = UsernameGenerator.generate(user.getEmail());
    }
  }

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(String lastName) {
    this.lastName = lastName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public void setProfileImageUrl(String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(String country) {
    this.country = country;
  }

  public String getState() {
    return state;
  }

  public void setState(String state) {
    this.state = state;
  }

  public String getAboutMe() {
    return aboutMe;
  }

  public void setAboutMe(String aboutMe) {
    this.aboutMe = aboutMe;
  }

  public User getUser() {
    return user;
  }

  public void setUser(User user) {
    this.user = user;
  }
} // Profile final class
