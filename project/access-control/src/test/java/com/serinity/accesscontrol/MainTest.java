// `MainTest` package name
package com.serinity.accesscontrol;

// `junit` static import(s)
import static org.junit.jupiter.api.Assertions.assertEquals;

// `junit` packages
import org.junit.jupiter.api.Test;

// `serinity` import(s)
import com.serinity.accesscontrol.util.SystemInfo;

/**
 * JUnit test class for basic system version checks.
 *
 * <p>
 * This class contains unit tests to verify the Java runtime and JavaFX versions
 * using
 * the {@link SystemInfo} utility class. It ensures that the expected versions
 * match the
 * actual environment values.
 * </p>
 *
 * <p>
 * Tests included:
 * </p>
 * <ul>
 * <li>{@link #testJavaVesion()} - Verifies the Java runtime version.</li>
 * <li>{@link #testJavafxVersion()} - Verifies the JavaFX version.</li>
 * </ul>
 *
 * <p>
 * These tests are primarily for sanity checks during build
 * </p>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see SystemInfo
 *
 *      <a href=
 *      "https://github.com/zouari-oss/serinity-desktop/blob/main/project/access-control/src/test/java/com/serinity/accesscontrol/MainTest.java">
 *      MainTest.java
 *      </a>
 */
public final class MainTest {
  @Test
  public void testJavaVesion() {
    assertEquals("25.0.2", SystemInfo.getJavaVersion());
  }

  @Test
  public void testJavafxVersion() {
    assertEquals("25.0.2", SystemInfo.getJavafxVersion());
  }
} // MainTest final test class
