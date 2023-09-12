package com.qunite.api.web.controller;

import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.ExceptionResponse;
import com.qunite.api.web.dto.auth.AuthenticationRequest;
import com.qunite.api.web.dto.auth.AuthenticationResponse;
import com.qunite.api.web.dto.auth.RefreshRequest;
import com.qunite.api.web.dto.user.UserCreationDto;
import com.qunite.api.web.mapper.AuthResponseMapper;
import com.qunite.api.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@CrossOrigin(origins = {"${client.web.url}"})
@RequiredArgsConstructor
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication controller")
@SecurityRequirements()
@RestController
public class AuthenticationController {
  private final UserService userService;
  private final UserMapper userMapper;
  private final AuthResponseMapper authResponseMapper;

  @Operation(summary = "Sign up user", description = "Create user", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "400", description = "User already exists",
          content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
  })
  @PostMapping("/sign-up")
  public ResponseEntity<Void> signUp(@Valid @RequestBody UserCreationDto userCreationDto) {
    userService.createOne(userMapper.toEntity(userCreationDto));
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Sign in by user credentials", description = "Login", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "403", description = "Invalid password",
          content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
      @ApiResponse(responseCode = "404", description = "User with given login does not exist",
          content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
  })
  @PostMapping("/sign-in")
  public ResponseEntity<AuthenticationResponse> signIn(
      @Valid @RequestBody AuthenticationRequest request) {
    AuthenticationResponse authenticationResponse = authResponseMapper.toAuthResponse(
        userService.signIn(request.getLogin(), request.getPassword()));

    return ResponseEntity.ok(authenticationResponse);
  }

  @Operation(summary = "Get new tokens by refresh token", description = "Refresh", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "403", description = "Invalid refresh token",
          content = @Content(schema = @Schema(implementation = ExceptionResponse.class))),
      @ApiResponse(responseCode = "404", description = "User with given id does not exist",
          content = @Content(schema = @Schema(implementation = ExceptionResponse.class)))
  })
  @PostMapping("/sign-in/refresh")
  public ResponseEntity<AuthenticationResponse> refresh(
      @Valid @RequestBody RefreshRequest request) {
    AuthenticationResponse authenticationResponse = authResponseMapper.toAuthResponse(
        userService.refreshTokens(request.getRefreshToken()));

    return ResponseEntity.ok(authenticationResponse);
  }
}