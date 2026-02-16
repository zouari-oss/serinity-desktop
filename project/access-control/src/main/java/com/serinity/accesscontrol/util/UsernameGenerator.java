// `UsernameGenerator` package name
package com.serinity.accesscontrol.util;

// `java` import(s)
import java.util.UUID;

/**
 * UsernameGenerator.java
 *
 * Utility class for generating unique, sanitized usernames based on email
 * addresses.
 *
 * <p>
 * This class provides a single static method {@link #generate(String)} which
 * takes an email address, removes any non-alphanumeric characters from the
 * local part, converts it to lowercase, and appends a short random UUID suffix
 * to ensure uniqueness.
 * </p>
 *
 * <p>
 * Designed to be used when creating
 * {@link com.serinity.accesscontrol.model.Profile} entities
 * to automatically generate immutable usernames.
 * </p>
 *
 * <p>
 * Note: The class is declared {@code final} and has no public constructor, as
 * it is a pure utility class.
 * </p>
 *
 *
 * <pre>
 * {@code
 * // Example usage
 * String username = UsernameGenerator.generate("John.Doe@example.com"); // Result might be "johndoe_a1b2c3"
 * }</pre>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.model.Profile
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/UsernameGenerator.java">
 *      UsernameGenerator.java
 *      </a>
 */
public final class UsernameGenerator {
  public static String generate(final String email) {
    return email.split("@")[0]
        .replaceAll("[^a-zA-Z0-9]", "")
        .toLowerCase() +
        "_" +
        UUID.randomUUID().toString().substring(0, 6);
  }
} // UsernameGenerator final class
