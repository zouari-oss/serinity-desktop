// `serinity` package name
package com.serinity.accesscontrol.util;

// `java` import(s)
import java.security.SecureRandom;
import java.util.Random;

/*
 * See https://medium.com/@dark.786.mode/creating-strong-passwords-made-easy-with-java-a-step-by-step-guide-5ab6833b07c6
 */
public class RandomPasswordGenerator {
  private static final String UPPER_CASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
  private static final String LOWER_CASE = "abcdefghijklmnopqrstuvwxyz";
  private static final String NUMBERS = "0123456789";
  private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+[]{}|;:,.<>?";
  private static final String ALL_CHARACTERS = UPPER_CASE + LOWER_CASE + NUMBERS + SPECIAL_CHARACTERS;
  private static final int DEFAULT_PASSWORD_LENGTH = 6;

  public static String generate() {
    return generate(DEFAULT_PASSWORD_LENGTH);
  }

  public static String generate(final int length) {
    final StringBuilder passwordBuilder = new StringBuilder();
    final Random random = new SecureRandom();

    // Fill the password with random characters from all sets.
    for (int i = 0; i < length; ++i) {
      final char randomChar = getRandomCharacter(ALL_CHARACTERS, random);
      passwordBuilder.append(randomChar);
    }

    return passwordBuilder.toString();
  }

  private static char getRandomCharacter(final String characterSet, final Random random) {
    return characterSet.charAt(random.nextInt(characterSet.length()));
  }
} // RandomPasswordGenerator final class
