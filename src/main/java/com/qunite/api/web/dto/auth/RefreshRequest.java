package com.qunite.api.web.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RefreshRequest {

  @NotBlank(message = "Specify token")
  String refreshToken;
}
