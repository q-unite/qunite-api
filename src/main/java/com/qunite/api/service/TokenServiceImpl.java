package com.qunite.api.service;

import com.qunite.api.data.TokenRepository;
import com.qunite.api.domain.Tokens;
import com.qunite.api.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor(onConstructor_ ={@Lazy})
public class TokenServiceImpl implements TokenService {
  private final TokenRepository tokenRepository;
  private final UserService userService;

  @Override
  @Transactional
  public boolean isTokenValid(String tokenValue) {
    return tokenRepository.existsByValue(tokenValue);
  }

  @Override
  @Transactional
  public void invalidate (String tokenValue) {
    tokenRepository.deleteByValue(tokenValue);
  }

  @Override
  @Transactional
  public Tokens create(Tokens tokens) {
    return tokenRepository.save(tokens);
  }

  @Override
  public void invalidateUserTokens(Long userId) {
    userService.findOne(userId).ifPresent(User::clearTokens);
  }
}
