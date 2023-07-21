package com.qunite.api.service;

import com.qunite.api.data.AccessTokenRepository;
import com.qunite.api.domain.AccessToken;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
  private final AccessTokenRepository tokenRepository;

  @Override
  @Transactional
  public boolean isTokenValid(String tokenValue) {
    return tokenRepository.findByValue(tokenValue).filter(AccessToken::isValid).isPresent();
  }

  @Override
  @Transactional
  public void invalidateToken(AccessToken token) {
    token.setValid(false);
    }
}
