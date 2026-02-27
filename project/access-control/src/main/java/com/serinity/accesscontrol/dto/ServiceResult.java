// `ServiceResult` package name
package com.serinity.accesscontrol.dto;

/**
 * Generic wrapper for service-layer operation results.
 *
 * <p>
 * Encapsulates a success/failure status, a human-readable message, and an
 * optional payload. Using this DTO instead of raw exceptions keeps controller
 * logic clean and avoids propagating checked exceptions across layers.
 * </p>
 *
 * <pre>{@code
 * ServiceResult<User> result = UserService.signIn(email, password);
 * if (result.isSuccess()) {
 *   User user = result.getData();
 * } else {
 *   System.err.println(result.getMessage());
 * }
 * }</pre>
 *
 * @param <T> the type of the optional payload
 *
 * @author @ZouariOmar (zouariomar20@gmail.com)
 * @version 1.0
 * @since 2026-02-03
 * @see com.serinity.accesscontrol.service.UserService
 *
 *      <a
 *      href=
 *      "https://github.com/zouari-oss/serinity-desktop/tree/main/project/access-control/src/main/java/com/serinity/accesscontrol/dto/ServiceResult.java">
 *      ServiceResult.java
 *      </a>
 */
public final class ServiceResult<T> {

  /**
   * Creates a successful result with a payload and a message.
   *
   * @param <T>     the payload type
   * @param data    the operation result data (may be {@code null})
   * @param message a human-readable success message
   * @return a successful {@link ServiceResult}
   */
  public static <T> ServiceResult<T> success(final T data, final String message) {
    return new ServiceResult<>(true, message, data);
  }

  /**
   * Creates a failure result with an error message and no payload.
   *
   * @param <T>     the payload type
   * @param message a human-readable error message
   * @return a failed {@link ServiceResult}
   */
  public static <T> ServiceResult<T> failure(final String message) {
    return new ServiceResult<>(false, message, null);
  }

  private final boolean success;
  private final String message;
  private final T data;

  private ServiceResult(final boolean success, final String message, final T data) {
    this.success = success;
    this.message = message;
    this.data = data;
  }

  /**
   * Returns whether the operation succeeded.
   *
   * @return {@code true} if the operation was successful
   */
  public boolean isSuccess() {
    return success;
  }

  /**
   * Returns the human-readable result message.
   *
   * @return success or error message
   */
  public String getMessage() {
    return message;
  }

  /**
   * Returns the optional result payload.
   *
   * @return the payload, or {@code null} for failure results
   */
  public T getData() {
    return data;
  }
} // ServiceResult final class
