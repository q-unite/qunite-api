package com.qunite.api.exception;

public class ForbiddenAccessException extends RuntimeException {
  public ForbiddenAccessException(String message) {
    super(message);
  }
}
