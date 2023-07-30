package com.qunite.api.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AuthenticationRequest {
  @NotBlank(message = "Specify login")
  private String login;
  @NotBlank(message = "Specify login")
  private String password;
}
