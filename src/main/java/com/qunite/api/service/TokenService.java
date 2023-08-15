package com.qunite.api.service;

import com.qunite.api.domain.TokenPair;

public interface TokenService {
  boolean isTokenValid(String tokenValue);

  void invalidate(String tokenValue);

  TokenPair create(TokenPair token);

  void invalidateUserTokens(Long userId);
}
