package com.qunite.api.web.dto.auth;

import lombok.Data;

@Data
public class LoginRequest {
 private String loginData;

 private String password;
}
