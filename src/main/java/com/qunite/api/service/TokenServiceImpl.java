package com.qunite.api.service;

import com.qunite.api.data.TokenRepository;
import com.qunite.api.domain.Tokens;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
  private final TokenRepository tokenRepository;

  @Override
  @Transactional
  public boolean isTokenValid(String tokenValue) {
    return tokenRepository.existsByValue(tokenValue);
  }

  @Override
  @Transactional
  public void delete(String tokenValue) {
    tokenRepository.deleteByValue(tokenValue);
  }

  @Override
  @Transactional
  public Tokens create(Tokens tokens) {
    return tokenRepository.save(tokens);
  }
}
