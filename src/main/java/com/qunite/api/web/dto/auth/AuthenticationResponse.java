package com.qunite.api.web.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthenticationResponse {
  private String accessToken;
  private String type;
  private String algorithm;
}
