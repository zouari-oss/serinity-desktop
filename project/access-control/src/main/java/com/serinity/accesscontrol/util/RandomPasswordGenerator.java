// `serinity` package name
package com.serinity.accesscontrol.util;

// `java` import(s)
import java.security.SecureRandom;

/*
 * See https://medium.com/@dark.786.mode/creating-strong-passwords-made-easy-with-java-a-step-by-step-guide-5ab6833b07c6
 */

/**
 * Utility class for generating cryptographically secure random passwords or
 * one-time codes.
 *
 * <p>
 * Uses {@link java.security.SecureRandom} to produce random character
 * sequences drawn from uppercase, lowercase, digit, and special-character
 * pools. The default length is 6 characters, intended for short-lived
 * one-time reset codes.
 * </p>
 *
 * <p>
 * NOTE: This class is not {@code final} by design; subclasses may extend it
 * to customise the character set or default length.
 * </p>
 *
 * <pre>{@code
 * String code = RandomPasswordGenerator.generate(); // 6 chars
 * String pwd = RandomPasswordGenerator.generate(12); // 12 chars
 * }</pre>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-16
 * @see com.serinity.accesscontrol.service.UserService
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/RandomPasswordGenerator.java">
 *      RandomPasswordGenerator.java
 *      </a>
 */
public class RandomPasswordGenerator {
  private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String NUMBERS = "0123456789";
  private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:,.<>?";
  private static final String ALL_CHARACTERS = UPPER_CASE + LOWER_CASE + NUMBERS + SPECIAL_CHARACTERS;
  private static final int DEFAULT_PASSWORD_LENGTH = 6;
  private static final SecureRandom RANDOM = new SecureRandom();

  /**
   * Generates a random password of the default length (6 characters).
   *
   * @return a randomly generated password string
   */
  public static String generate() {
    return generate(DEFAULT_PASSWORD_LENGTH);
  }

  /**
   * Generates a random password of the specified length.
   *
   * @param length the number of characters in the generated password
   * @return a randomly generated password string
   */
  public static String generate(final int length) {
    final StringBuilder passwordBuilder = new StringBuilder();

    // Fill the password with random characters from all sets.
    for (int i = 0; i < length; ++i) {
      final char randomChar = getRandomCharacter(ALL_CHARACTERS, RANDOM);
      passwordBuilder.append(randomChar);
    }

    return passwordBuilder.toString();
  }

  private static char getRandomCharacter(final String characterSet, final SecureRandom random) {
    return characterSet.charAt(random.nextInt(characterSet.length()));
  }
} // RandomPasswordGenerator final class
