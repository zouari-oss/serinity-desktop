// `SystemInfo` package name
package com.serinity.accesscontrol.util;

/**
 * System information class
 *
 * <p>
 * Funcionalies:
 * </p>
 * <ul>
 * <li>{@link com.serinity.accesscontrol.util.SystemInfo#getJavafxVersion} -
 * Return the current javafx version</li>
 * <li>{@link com.serinity.accesscontrol.util.SystemInfo#getJavaVersion} -
 * Return the current java version</li>
 * <li>{@link com.serinity.accesscontrol.util.SystemInfo#getPrivateIpAddress} -
 * Return user up interface private ip address (not localhost)</li>
 * <li>{@link com.serinity.accesscontrol.util.SystemInfo#getMacAddress} - Return
 * user up interface mac address</li>
 * </ul>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-01-28
 *
 *        <a
 *        href=
 *        "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/SystemInfo.java">
 *        SystemInfo.java
 *        </a>
 */
public final class SystemInfo {
  public static String getJavaVersion() {
    return System.getProperty("java.version");
  }

  public static String getJavafxVersion() {
    return System.getProperty("javafx.version");
  }

  /**
   * Get the first active non-loopback(localhost) IPv4 address of the machine
   */
  public static String getPrivateIpAddress() {
    try {
      final var interfaces = java.net.NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        final var ni = interfaces.nextElement();
        if (ni.isLoopback() || !ni.isUp())
          continue;

        final var addresses = ni.getInetAddresses();
        while (addresses.hasMoreElements()) {
          final var addr = addresses.nextElement();
          if (addr instanceof java.net.Inet4Address && !addr.isLoopbackAddress()) {
            return addr.getHostAddress();
          }
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return "unknown";
  }

  /**
   * Get the MAC address of the first active network interface
   *
   * Source - https://stackoverflow.com/a/30879040
   * Posted by BullyWiiPlaza
   * Retrieved 2026-02-04, License - CC BY-SA 3.0
   */
  public static String getMacAddress() {
    try {
      final var interfaces = java.net.NetworkInterface.getNetworkInterfaces();
      while (interfaces.hasMoreElements()) {
        final var ni = interfaces.nextElement();
        if (ni.isLoopback() || !ni.isUp())
          continue;

        final byte[] mac = ni.getHardwareAddress();
        if (mac != null) {
          final var sb = new StringBuilder();
          for (int i = 0; i < mac.length; i++) {
            sb.append(String.format("%02X%s", mac[i], (i < mac.length - 1) ? "-" : ""));
          }
          return sb.toString();
        }
      }
    } catch (final Exception e) {
      e.printStackTrace();
    }

    return "unknown";
  }

  private SystemInfo() {
  }
} // SystemInfo final class
