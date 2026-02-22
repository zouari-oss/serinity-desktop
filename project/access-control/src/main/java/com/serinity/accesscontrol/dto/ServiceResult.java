package com.serinity.accesscontrol.dto;

public class ServiceResult<T> {
  public static <T> ServiceResult<T> success(final T data, final String message) {
    return new ServiceResult<>(true, message, data);
  }

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

  public boolean isSuccess() {
    return success;
  }

  public String getMessage() {
    return message;
  }

  public T getData() {
    return data;
  }
}
