package com.qunite.api.service;

import com.qunite.api.data.TokenRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.TokenPair;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
  private final TokenRepository tokenRepository;
  private final UserRepository userRepository;

  @Override
  @Transactional
  public Optional<TokenPair> findByValue(String tokenValue) {
    return tokenRepository.findByValue(tokenValue);
  }

  @Override
  @Transactional
  public boolean isTokenValid(String tokenValue) {
    return tokenRepository.findByValue(tokenValue).filter(TokenPair::isValid).isPresent();
  }

  @Override
  @Transactional
  public void invalidate(String tokenValue) {
    tokenRepository.findByValue(tokenValue).ifPresent(tokenPair -> tokenPair.setValid(false));
  }

  @Override
  @Transactional
  public TokenPair create(TokenPair tokenPair) {
    return tokenRepository.save(tokenPair);
  }

  @Override
  @Transactional
  public void invalidateUserTokens(Long userId) {
    userRepository.findById(userId).ifPresent(user -> user.getTokenPairs().forEach(
        tokenPair -> tokenPair.setValid(false)
    ));
  }
}
