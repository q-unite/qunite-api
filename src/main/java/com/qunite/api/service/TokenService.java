package com.qunite.api.service;

import com.qunite.api.domain.Tokens;

public interface TokenService {
  boolean isTokenValid(String tokenValue);

  void invalidate(String tokenValue);

  Tokens create(Tokens token);

  void invalidateUserTokens(Long userId);
}
