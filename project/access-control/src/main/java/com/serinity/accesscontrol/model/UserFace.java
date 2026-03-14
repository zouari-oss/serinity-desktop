// `UserFace` package name
package com.serinity.accesscontrol.model;

// `zouarioss` import(s)
import org.zouarioss.skinnedratorm.annotations.Column;
import org.zouarioss.skinnedratorm.annotations.Entity;
import org.zouarioss.skinnedratorm.annotations.JoinColumn;
import org.zouarioss.skinnedratorm.annotations.OneToOne;
import org.zouarioss.skinnedratorm.annotations.Table;

// `serinity` import(s)
import com.serinity.accesscontrol.model.base.TimestampedEntity;

/**
 * Represents a user's face biometric data stored in the system.
 *
 * <p>
 * This entity holds a serialized ArcFace embedding vector for a specific
 * {@link com.serinity.accesscontrol.model.User}, used for face recognition
 * during authentication.
 * </p>
 *
 * <p>
 * The table {@code user_faces} enforces a one-to-one relationship with
 * the {@code users} table via a unique {@code user_id} constraint.
 * </p>
 *
 * <p>
 * Fields include:
 * </p>
 * <ul>
 * <li>{@code user} - The {@link com.serinity.accesscontrol.model.User} this
 * face embedding belongs to.</li>
 * <li>{@code embedding} - Serialized 512-dimensional ArcFace float vector
 * stored as binary ({@code LONGBLOB}).</li>
 * </ul>
 *
 * <p>
 * Note: This class is declared {@code final} to prevent inheritance and ensure
 * data integrity.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-27
 * @see com.serinity.accesscontrol.model.User
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/model/UserFace.java">
 *      UserFace.java
 *      </a>
 */
@Entity
@Table(name = "user_faces")
public final class UserFace extends TimestampedEntity {

  @OneToOne(optional = false)
  @JoinColumn(name = "user_id", nullable = false, unique = true)
  private User user;

  /**
   * Serialized face embedding vector (ArcFace 512-d float array)
   * Stored as binary for efficiency.
   */
  @Column(name = "embedding", nullable = false, columnDefinition = "LONGBLOB")
  private byte[] embedding;

  // ######################
  // ### CONSTRUCTOR(s) ###
  // ######################

  public UserFace() {
  }

  public UserFace(final User user, final byte[] embedding) {
    this.user = user;
    this.embedding = embedding;
  }

  // #########################
  // ### GETTERS & SETTERS ###
  // #########################

  public User getUser() {
    return user;
  }

  public void setUser(final User user) {
    this.user = user;
  }

  public byte[] getEmbedding() {
    return embedding;
  }

  public void setEmbedding(final byte[] embedding) {
    this.embedding = embedding;
  }
} // `UserFace` final class
