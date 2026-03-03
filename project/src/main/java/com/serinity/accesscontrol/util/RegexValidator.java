
// `RegexValidator` package name
package com.serinity.accesscontrol.util;

// `java` import(s)
import java.util.regex.Pattern;

/**
 * Utility class for validating input values using regular expressions.
 *
 * <p>
 * Provides reusable validation logic for:
 * </p>
 * <ul>
 * <li>Email addresses</li>
 * <li>Phone number</li>
 * <li>Passwords (strong policy)</li>
 * </ul>
 *
 * <p>
 * NOTE: This class is immutable and cannot be instantiated.
 * </p>
 *
 * @author @ZouariOmar
 * @version 1.0
 * @since 2026-02-17
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/RegexValidator.java">
 *        RegexValidator.java
 *        </a>
 */
public final class RegexValidator {
  private static final Pattern EMAIL_PATTERN = Pattern.compile(
      "^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

  private static final Pattern PHONE_NUMBER_PATTERN = Pattern.compile(
      "^\\+?[0-9]{7,15}$" // Allows optional '+' and 7-15 digits
  );

  /*
   * INFO: At least:
   * - 8 character
   * - 1 uppercase
   * - 1 lowercase
   * - 1 digit
   * - 1 special character
   */
  private static final Pattern PASSWORD_PATTERN = Pattern.compile(
      "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

  public static boolean isValidEmail(final String email) {
    return email != null && EMAIL_PATTERN.matcher(email).matches();
  }

  public static boolean isValidPhoneNumber(final String phoneNumber) {
    return phoneNumber != null && PHONE_NUMBER_PATTERN.matcher(phoneNumber).matches();
  }

  public static boolean isValidPassword(final String password) {
    return password != null && PASSWORD_PATTERN.matcher(password).matches();
  }
} // RegexValidator final class
