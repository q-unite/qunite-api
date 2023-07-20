package com.qunite.api.service;

import com.qunite.api.data.AccessTokenRepository;
import com.qunite.api.domain.AccessToken;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TokenServiceImpl implements TokenService {
  AccessTokenRepository tokenRepository;

  @Override
  @Transactional
  public boolean isTokenValid(String tokenValue) {
    return tokenRepository.findByValue(tokenValue).filter(AccessToken::isValid).isPresent();
  }

  @Override
  @Transactional
  public void invalidateToken(AccessToken token) {
    token.setValid(false);
    tokenRepository.save(token);
  }
}
