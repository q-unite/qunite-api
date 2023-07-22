package com.qunite.api.exception;

public class UserNotFoundException extends EntityNotFoundException {
  public UserNotFoundException(String message) {
    super(message);
  }
}
