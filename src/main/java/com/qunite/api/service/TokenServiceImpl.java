package com.qunite.api.service;

import com.qunite.api.data.TokenRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.TokenPair;
import com.qunite.api.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
  private final TokenRepository tokenRepository;
  private final UserRepository userRe;

  @Override
  @Transactional
  public boolean isTokenValid(String tokenValue) {
    return tokenRepository.existsByValue(tokenValue);
  }

  @Override
  @Transactional
  public void invalidate(String tokenValue) {
    tokenRepository.deleteByValue(tokenValue);
  }

  @Override
  @Transactional
  public TokenPair create(TokenPair tokenPair) {
    return tokenRepository.save(tokenPair);
  }

  @Override
  @Transactional
  public void invalidateUserTokens(Long userId) {
    userRe.findById(userId).ifPresent(User::clearTokens);
  }
}
