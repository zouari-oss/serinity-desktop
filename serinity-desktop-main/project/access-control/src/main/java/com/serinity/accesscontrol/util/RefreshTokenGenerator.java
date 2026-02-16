/**
 * RefreshTokenGenerator.java
 *
 * We use this class to generate new refrech token for the current session
 *
 * <p>none</p>
 *
 * @author  @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since   2026-02-04
 *
 * <a
 * href="https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/util/RefreshTokenGenerator.java"
 * target="_blank">
 * RefreshTokenGenerator.java
 * </a>
 */

// `RefreshTokenGenerator` package name
package com.serinity.accesscontrol.util;

// `java` import(s)
import java.security.SecureRandom;
import java.util.Base64;

public final class RefreshTokenGenerator {
  private static final SecureRandom RANDOM = new SecureRandom();

  public static String generate() {
    final byte[] bytes = new byte[64]; // 512 bits
    RANDOM.nextBytes(bytes);
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
  }
} // RefreshTokenGenerator class
