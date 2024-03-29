package com.qunite.api.web;

import com.auth0.jwt.exceptions.JWTVerificationException;
import com.qunite.api.exception.EntryNotFoundException;
import com.qunite.api.exception.ForbiddenAccessException;
import com.qunite.api.exception.InvalidPasswordException;
import com.qunite.api.exception.InvalidRefreshTokenException;
import com.qunite.api.exception.QueueNotFoundException;
import com.qunite.api.exception.UserAlreadyExistsException;
import com.qunite.api.exception.UserNotFoundException;
import com.qunite.api.web.dto.ExceptionResponse;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ResponseEntityControllerAdvice {
  @ExceptionHandler({
      QueueNotFoundException.class,
      UserNotFoundException.class,
      EntryNotFoundException.class})
  public ResponseEntity<ExceptionResponse> handleNotFound(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(exceptionResponse(exception.getMessage()));
  }

  @ExceptionHandler(UserAlreadyExistsException.class)
  public ResponseEntity<ExceptionResponse> handleBadRequest(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
        .body(exceptionResponse(exception.getMessage()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<Map<String, List<String>>> handleValidationError(
      MethodArgumentNotValidException exception) {
    var errors = exception.getFieldErrors().stream()
        .filter(fe -> fe.getDefaultMessage() != null)
        .collect(Collectors.groupingBy(FieldError::getField,
            Collectors.mapping(FieldError::getDefaultMessage, Collectors.toList())));
    return ResponseEntity.badRequest().body(errors);
  }

  @ExceptionHandler({
      ForbiddenAccessException.class,
      InvalidPasswordException.class,
      InvalidRefreshTokenException.class,
      JWTVerificationException.class})
  public ResponseEntity<ExceptionResponse> handleForbidden(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.FORBIDDEN)
        .body(exceptionResponse(exception.getMessage()));
  }

  @ExceptionHandler(CannotAcquireLockException.class)
  public ResponseEntity<ExceptionResponse> handleConflict(RuntimeException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(exceptionResponse(exception.getMessage()));
  }

  private ExceptionResponse exceptionResponse(String message) {
    return new ExceptionResponse(message,
        LocalDateTime.now().format(DateTimeFormatter.ofPattern("hh:mm:ss")));
  }
}
