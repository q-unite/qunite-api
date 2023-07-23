package com.qunite.api.web.controller;

import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.auth.AuthenticationRequest;
import com.qunite.api.web.dto.auth.AuthenticationResponse;
import com.qunite.api.web.dto.user.UserCreationDto;
import com.qunite.api.web.mapper.AuthResponseMapper;
import com.qunite.api.web.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication controller")
@SecurityRequirements()
@RestController
public class AuthenticationController {
  private final UserService userService;
  private final AuthResponseMapper responseMapper;
  private final UserMapper userMapper;

  @Operation(summary = "Sign up user", responses = @ApiResponse(responseCode = "200"))
  @PostMapping("/sign-up")
  public ResponseEntity<Void> signUp(@Valid @RequestBody UserCreationDto userCreationDto) {
    userService.createOne(userMapper.toEntity(userCreationDto));
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Sign in by user credentials", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())})
  @PostMapping("/sign-in")
  public ResponseEntity<AuthenticationResponse> signIn(
      @Valid @RequestBody AuthenticationRequest request) {
    return ResponseEntity.of(userService.signIn(request.getLogin(), request.getPassword())
        .map(responseMapper::toAuthResponse));
  }
}
