package com.qunite.api.exception;

public class QueueNotFoundException extends EntityNotFoundException {
  public QueueNotFoundException(String message) {
    super(message);
  }
}
