// `Profile` package name
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
 * Represents a user's profile in the system. Contains personal information,
 * contact details, and metadata related to the user's account. Each
 * {@link Profile} is associated with exactly one
 * {@link com.serinity.accesscontrol.model.User}.
 *
 * <p>
 * The {@code Profile} class extends
 * {@link com.serinity.accesscontrol.model.base.TimestampedEntity},
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
  /** Creates an empty user profile entity. */
  public Profile() {
  }
  @Column(nullable = false, updatable = false)
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

  @Column(name = "anime_avatar_image_url", length = 512, nullable = true)
  private String animeAvatarImageUrl;

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

  /**
   * Returns the unique username.
   *
   * @return username value
   */
  public String getUsername() {
    return username;
  }

  /**
   * Sets the unique username.
   *
   * @param username username value
   */
  public void setUsername(final String username) {
    this.username = username;
  }

  /**
   * Returns the first name.
   *
   * @return first name
   */
  public String getFirstName() {
    return firstName;
  }

  /**
   * Sets the first name.
   *
   * @param firstName first name
   */
  public void setFirstName(final String firstName) {
    this.firstName = firstName;
  }

  /**
   * Returns the last name.
   *
   * @return last name
   */
  public String getLastName() {
    return lastName;
  }

  /**
   * Sets the last name.
   *
   * @param lastName last name
   */
  public void setLastName(final String lastName) {
    this.lastName = lastName;
  }

  /**
   * Returns the phone number.
   *
   * @return phone number
   */
  public String getPhone() {
    return phone;
  }

  /**
   * Sets the phone number.
   *
   * @param phone phone number
   */
  public void setPhone(final String phone) {
    this.phone = phone;
  }

  /**
   * Returns the gender.
   *
   * @return gender value
   */
  public Gender getGender() {
    return gender;
  }

  /**
   * Sets the gender.
   *
   * @param gender gender value
   */
  public void setGender(final Gender gender) {
    this.gender = gender;
  }

  /**
   * Returns the profile image URL.
   *
   * @return profile image URL
   */
  public String getProfileImageUrl() {
    return profileImageUrl;
  }

  /**
   * Sets the profile image URL.
   *
   * @param profileImageUrl profile image URL
   */
  public void setProfileImageUrl(final String profileImageUrl) {
    this.profileImageUrl = profileImageUrl;
  }

  /**
   * Returns the anime avatar image URL.
   *
   * @return anime avatar image URL
   */
  public String getAnimeAvatarImageUrl() {
    return animeAvatarImageUrl;
  }

  /**
   * Sets the anime avatar image URL.
   *
   * @param animeAvatarImageUrl anime avatar image URL
   */
  public void setAnimeAvatarImageUrl(final String animeAvatarImageUrl) {
    this.animeAvatarImageUrl = animeAvatarImageUrl;
  }

  /**
   * Returns the country.
   *
   * @return country value
   */
  public String getCountry() {
    return country;
  }

  /**
   * Sets the country.
   *
   * @param country country value
   */
  public void setCountry(final String country) {
    this.country = country;
  }

  /**
   * Returns the state/region.
   *
   * @return state or region
   */
  public String getState() {
    return state;
  }

  /**
   * Sets the state/region.
   *
   * @param state state or region
   */
  public void setState(final String state) {
    this.state = state;
  }

  /**
   * Returns the short biography text.
   *
   * @return about-me text
   */
  public String getAboutMe() {
    return aboutMe;
  }

  /**
   * Sets the short biography text.
   *
   * @param aboutMe about-me text
   */
  public void setAboutMe(final String aboutMe) {
    this.aboutMe = aboutMe;
  }

  /**
   * Returns the owning user.
   *
   * @return owning user
   */
  public User getUser() {
    return user;
  }

  /**
   * Sets the owning user.
   *
   * @param user owning user
   */
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
