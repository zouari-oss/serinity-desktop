// `User` package name
package com.serinity.accesscontrol.model;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.annotations.Column;
import org.zouarioss.skinnedratorm.annotations.Entity;
import org.zouarioss.skinnedratorm.annotations.Enumerated;
import org.zouarioss.skinnedratorm.annotations.JoinColumn;
import org.zouarioss.skinnedratorm.annotations.OneToOne;
import org.zouarioss.skinnedratorm.annotations.PrePersist;
import org.zouarioss.skinnedratorm.annotations.Table;
import org.zouarioss.skinnedratorm.annotations.UniqueConstraint;
import org.zouarioss.skinnedratorm.flag.CascadeType;
import org.zouarioss.skinnedratorm.flag.EnumType;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.Gender;
import com.serinity.accesscontrol.model.base.TimestampedEntity;
import com.serinity.accesscontrol.util.UsernameGenerator;

/**
 * Profile.java
 *
 * Represents a user's profile in the system. Contains personal information,
 * contact details, and metadata related to the user's account. Each
 * {@link Profile} is associated with exactly one
 * {@link com.serinity.accesscontrol.model.User}.
 *
 * <p>
 * The {@code Profile} class extends
 * {@link com.serinity.accesscontrol.model.TimestampedEntity},
 * which automatically provides {@code createdAt} and {@code updatedAt}
 * timestamps.
 * </p>
 *
 * <p>
 * Fields include:
 * </p>
 * <ul>
 * <li>{@code firstName} - The user's first name (optional).</li>
 * <li>{@code lastName} - The user's last name (optional).</li>
 * <li>{@code phone} - The user's phone number (optional).</li>
 * <li>{@code country} - The user's country (optional).</li>
 * <li>{@code state} - The user's state/region (optional).</li>
 * <li>{@code aboutMe} - A short biography or description (optional).</li>
 * <li>{@code username} - Auto-generated unique username, immutable after
 * creation.</li>
 * <li>{@code profileImageUrl} - Optional URL to the user's profile image.</li>
 * <li>{@code user} - The {@link com.serinity.accesscontrol.model.User} this
 * profile belongs to.</li>
 * </ul>
 *
 * <p>
 * NOTE: The {@code username} field is generated automatically when the
 * profile is created and cannot be modified manually. This ensures uniqueness
 * across the system.
 * </p>
 *
 * <p>
 * NOTE: This class is declared {@code final} to prevent inheritance and ensure
 * session integrity.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.User
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/Profile.java">
 *      Profile.java
 *      </a>
 */
@Entity
@Table(name = "profiles")
@UniqueConstraint(name = "uk_profile_username", columnNames = "username")
public final class Profile extends TimestampedEntity {
  @Column(nullable = false, length = 50, updatable = false)
  private String username; // Pre-persist

  @Column(nullable = true)
  private String firstName;

  @Column(nullable = true)
  private String lastName;

  @Column(length = 20, nullable = true)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(length = 10, nullable = true)
  private Gender gender;

  @Column(name = "profile_image_url", length = 512, nullable = true)
  private String profileImageUrl;

  @Column(length = 100, nullable = true)
  private String country; // e.g., Tunisia, Marroc, ..

  @Column(length = 100, nullable = true)
  private String state; // e.g., Sfax, Tunis, ..

  @Column(length = 500, nullable = true)
  private String aboutMe;

  @OneToOne(cascade = CascadeType.ALL)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################

  public String getUsername() {
    return username;
  }

  public void setUsername(final String username) {
    this.username = username;
  }

  public String getFirstName() {
    return firstName;
  }

  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  public String getLastName() {
    return lastName;
  }

  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(final String phone) {
    this.phone = phone;
  }

  public Gender getGender() {
    return gender;
  }

  public void setGender(final Gender gender) {
    this.gender = gender;
  }

  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  public void setProfileImageUrl(final String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  public String getCountry() {
    return country;
  }

  public void setCountry(final String country) {
    this.country = country;
  }

  public String getState() {
    return state;
  }

  public void setState(final String state) {
    this.state = state;
  }

  public String getAboutMe() {
    return aboutMe;
  }

  public void setAboutMe(final String aboutMe) {
    this.aboutMe = aboutMe;
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

  /**
   * Auto-generate username ONCE.
   * Immutable after creation.
   */
  @PrePersist
  private void onCreate() {
    if (this.username == null) {
      this.username = UsernameGenerator.generate(user.getEmail());
    }
  }
} // Profile final class
