// `StatusMessageProvider` package name
package com.serinity.accesscontrol.controller.base;

// `serinity` import(s)
import com.serinity.accesscontrol.flag.MessageStatus;

/**
 * Provides a mechanism for displaying status messages to the user.
 *
 * <p>
 * Implementing controllers can use this interface to show messages
 * with visual styling and optional icons, such as success, error,
 * warning, or informational messages.
 * </p>
 *
 * <p>
 * Example usage:
 * </p>
 *
 * <pre>{@code
 * showStatusMessage("Operation successful!", MessageStatus.SUCCESS);
 * showStatusMessage("Failed to save changes.", MessageStatus.ERROR);
 * }</pre>
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-21
 * @see MessageStatus
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/controller/base/StackNavigable.java">
 *      StackNavigable.java
 *      </a>
 */
public interface StatusMessageProvider {

  /**
   * Shows a status message on the UI.
   *
   * @param message the text to display
   * @param status  the type of message (SUCCESS, ERROR, WARNING, INFO)
   */
  void showStatusMessage(String message, MessageStatus status);
} // StatusMessageProvider interface
