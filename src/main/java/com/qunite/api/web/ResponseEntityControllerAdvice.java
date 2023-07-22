package com.qunite.api.web;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.qunite.api.exception.EntityNotFoundException;
import com.qunite.api.exception.ForbiddenAccessException;
import com.qunite.api.exception.UserAlreadyExistsException;
import com.qunite.api.web.dto.ExceptionResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class ResponseEntityControllerAdvice {
  @ExceptionHandler({
      UserAlreadyExistsException.class,
      IllegalArgumentException.class,
      HttpMessageNotReadableException.class,
      MethodArgumentTypeMismatchException.class})
  public ResponseEntity<ExceptionResponse> handleBadRequest(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(exceptionResponse(exception.getMessage()));
  }

  @ExceptionHandler(JWTDecodeException.class)
  public ResponseEntity<ExceptionResponse> handleUnauthorized(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(exceptionResponse(exception.getMessage()));
  }

  @ExceptionHandler(ForbiddenAccessException.class)
  public ResponseEntity<ExceptionResponse> handleForbidden(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(exceptionResponse(exception.getMessage()));
  }

  @ExceptionHandler(EntityNotFoundException.class)
  public ResponseEntity<ExceptionResponse> handleNotFound(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(exceptionResponse(exception.getMessage()));
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  public ResponseEntity<ExceptionResponse> handleConflict(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(exceptionResponse(exception.getMessage()));
  }

  private ExceptionResponse exceptionResponse(String message) {
    return new ExceptionResponse(message,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")));
  }
}
