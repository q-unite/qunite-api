package com.qunite.api.exception;

public class UserForbiddenException extends RuntimeException{
  public UserForbiddenException(String message) {
    super(message);
  }
}
