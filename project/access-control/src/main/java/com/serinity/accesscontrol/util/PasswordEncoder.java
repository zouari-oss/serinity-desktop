// `PasswordEncoder` package name
package com.serinity.accesscontrol.util;

// `springframework` import(s)
import org.springframework.security.crypto.bcrypt.BCrypt;

/**
 * Utility class for securely encoding and verifying passwords.
 *
 * <p>
 * This class provides static methods to encode plain-text passwords using
 * the {@link BCrypt} hashing algorithm and to verify passwords against their
 * hashed counterparts.
 * </p>
 *
 * <p>
 * <strong>Important:</strong> This class is {@code final} and has a private
 * constructor to prevent instantiation, as it is intended to be a pure
 * utility class.
 * </p>
 *
 * <pre>{@code
 * // Example usage
 * String hashed = PasswordEncoder.encode("mySecretPassword");
 * boolean matches = PasswordEncoder.isConfirmPassword("mySecretPassword", hashed);
 * }</pre>
 *
 * <p>
 * This class is suitable for storing password hashes securely in databases
 * and for verifying user-provided passwords during authentication.
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.1
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.User
 *
 *      <a href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/PasswordEncoder.java">
 *      PasswordEncoder.java
 *      </a>
 */
public final class PasswordEncoder {

  /**
   * Encodes a plain-text password using the BCrypt hashing algorithm.
   *
   * @param passwd the plain-text password to encode; must not be {@code null}
   * @return the hashed password as a {@link String}
   */
  public static final String encode(final String passwd) {
    return BCrypt.hashpw(passwd, BCrypt.gensalt());
  }

  /**
   * Verifies if a plain-text password matches a previously encoded hash.
   *
   * @param passwd        the plain-text password to verify
   * @param confirmPasswd the previously hashed password
   * @return {@code true} if the plain-text password matches the hash,
   *         {@code false} otherwise
   */
  public static final boolean isConfirmPassword(final String passwd, final String confirmPasswd) {
    return BCrypt.checkpw(passwd, confirmPasswd);
  }
} // PasswordEncoder final class
