package com.qunite.api.web.controller;

import com.qunite.api.service.UserService;
import com.qunite.api.web.dto.auth.AuthenticationRequest;
import com.qunite.api.web.dto.auth.AuthenticationResponse;
import com.qunite.api.web.dto.user.UserCreationDto;
import com.qunite.api.web.mapper.AuthResponseMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
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

@CrossOrigin
@RequiredArgsConstructor
@RequestMapping(path = "/auth", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Authentication controller")
@RestController
public class AuthenticationController {
  private final UserService userService;
  private final AuthResponseMapper responseMapper;

  @Operation(summary = "Register user", responses = @ApiResponse(responseCode = "200"))
  @PostMapping("/register")
  public ResponseEntity<Void> register(@Valid @RequestBody UserCreationDto userCreationDto) {
    userService.register(
        userCreationDto.getUsername(), userCreationDto.getEmail(), userCreationDto.getPassword()
    );
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Sign in by user credentials", responses = {
      @ApiResponse(responseCode = "200"),
      @ApiResponse(responseCode = "404", content = @Content())})
  @PostMapping("/sign-in")
  public ResponseEntity<AuthenticationResponse> signIn(
      @Valid @RequestBody AuthenticationRequest request) {
    return ResponseEntity.of(userService.signIn(request.getLoginData(), request.getPassword())
        .map(responseMapper::toAuthResponse));
  }


}