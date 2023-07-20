package com.qunite.api.service;

import com.qunite.api.domain.AccessToken;

public interface TokenService {
  boolean isTokenValid(String tokenValue);

  void invalidateToken(AccessToken token);
}
