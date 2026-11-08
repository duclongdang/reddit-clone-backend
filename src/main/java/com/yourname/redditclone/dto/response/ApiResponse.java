package com.yourname.redditclone.dto.response;

import java.time.Instant;

/**
 * Generic API response wrapper. Use ApiResponse.ok(data) for successful responses and
 * ApiResponse.error(error) for errors.
 */
public class ApiResponse<T> {
  private boolean success;
  private T data;
  private ErrorResponse error;
  private Meta meta;
  private Instant timestamp = Instant.now();

  public ApiResponse() {}

  public ApiResponse(boolean success, T data, ErrorResponse error, Meta meta) {
    this.success = success;
    this.data = data;
    this.error = error;
    this.meta = meta;
    this.timestamp = Instant.now();
  }

  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, data, null, null);
  }

  public static <T> ApiResponse<T> ok(T data, Meta meta) {
    return new ApiResponse<>(true, data, null, meta);
  }

  public static <T> ApiResponse<T> error(ErrorResponse error) {
    return new ApiResponse<>(false, null, error, null);
  }

  // getters / setters
  public boolean isSuccess() {
    return success;
  }

  public void setSuccess(boolean success) {
    this.success = success;
  }

  public T getData() {
    return data;
  }

  public void setData(T data) {
    this.data = data;
  }

  public ErrorResponse getError() {
    return error;
  }

  public void setError(ErrorResponse error) {
    this.error = error;
  }

  public Meta getMeta() {
    return meta;
  }

  public void setMeta(Meta meta) {
    this.meta = meta;
  }

  public Instant getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(Instant timestamp) {
    this.timestamp = timestamp;
  }

  public static class Meta {
    private String message;
    private String code;
    private Object extra;

    public Meta() {}

    public Meta(String message, String code) {
      this.message = message;
      this.code = code;
    }

    public String getMessage() {
      return message;
    }

    public void setMessage(String message) {
      this.message = message;
    }

    public String getCode() {
      return code;
    }

    public void setCode(String code) {
      this.code = code;
    }

    public Object getExtra() {
      return extra;
    }

    public void setExtra(Object extra) {
      this.extra = extra;
    }
  }
}
