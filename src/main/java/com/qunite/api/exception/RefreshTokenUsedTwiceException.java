package com.qunite.api.exception;

public class RefreshTokenUsedTwiceException extends RuntimeException {
  public RefreshTokenUsedTwiceException(String message) {
    super(message);
  }
}
