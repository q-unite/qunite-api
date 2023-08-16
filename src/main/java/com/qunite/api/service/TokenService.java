package com.qunite.api.service;

import com.qunite.api.domain.TokenPair;
import java.util.Optional;

public interface TokenService {
  Optional<TokenPair> findByValue(String tokenValue);

  boolean isTokenValid(String tokenValue);

  void invalidate(String tokenValue);

  TokenPair create(TokenPair token);

  void invalidateUserTokens(Long userId);
}
