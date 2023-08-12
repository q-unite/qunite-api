package com.qunite.api.web.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationResponse {
  private String accessToken;

  private String refreshToken;
  private String type;
  private String algorithm;
}
