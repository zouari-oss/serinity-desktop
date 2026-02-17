// `MainTest` package name
package com.serinity.moodcontrol;

// `junit` packages
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MainTest {
  @Test
  public void testJavaVesion() {
    assertEquals("25.0.1", SystemInfo.javaVersion());
  }

  @Test
  public void testJavafxVersion() {
    assertEquals("25.0.2", SystemInfo.javafxVersion());
  }
} // MainTest class
