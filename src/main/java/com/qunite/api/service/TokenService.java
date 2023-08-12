package com.qunite.api.service;

import com.qunite.api.domain.Tokens;

public interface TokenService {
  boolean isTokenValid(String tokenValue);

  void deleteOne(String tokenValue);

  Tokens createOne(Tokens token);
}
