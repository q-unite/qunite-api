package com.qunite.api.web.dto.auth;

import lombok.Data;

  @Data
public class AuthenticationRequest {
  private String login;
  private String password;
}
