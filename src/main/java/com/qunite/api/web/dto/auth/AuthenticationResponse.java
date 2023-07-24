package com.qunite.api.web.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
public class AuthenticationResponse {
  private String token;
  private String type;
  private String algorithm;
}
