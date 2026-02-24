package com.serinity.exercicecontrol;

public class SystemInfo {
  public static String javaVersion() {
    return System.getProperty("java.version");
  }

  public static String javafxVersion() {
    return System.getProperty("javafx.version");
  }
} // SystemInfo class
