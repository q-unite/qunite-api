package com.qunite.api.service;

import com.qunite.api.domain.Tokens;
import java.util.Optional;

public interface TokenService {
  boolean isTokenValid(String tokenValue);

  void invalidateTokens(Tokens tokens);

  void deleteOne(String tokenValue);

  Tokens createOne(Tokens token);
}
