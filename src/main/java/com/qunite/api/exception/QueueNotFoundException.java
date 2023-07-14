package com.qunite.api.exception;

public class QueueNotFoundException extends RuntimeException {
  public QueueNotFoundException(String message) {
    super(message);
  }
}
