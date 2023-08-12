package com.qunite.api.service;

import ch.qos.logback.core.subst.Token;
import com.qunite.api.data.TokenRepository;
import com.qunite.api.domain.Tokens;
import java.util.Optional;
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
  public void invalidateTokens(Tokens tokens) {
    tokenRepository.delete(tokens);
    }

  @Override
  public void deleteOne(String tokenValue) {
    tokenRepository.deleteByValue(tokenValue);
  }

  @Override
  public Tokens createOne(Tokens tokens) {
    return tokenRepository.save(tokens);
  }
}
