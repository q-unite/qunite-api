package com.qunite.api.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.qunite.api.annotation.IntegrationTest;
import com.qunite.api.data.EntryRepository;
import com.qunite.api.data.QueueRepository;
import com.qunite.api.data.TokenRepository;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.TokenPair;
import com.qunite.api.utils.JpaRepositoryUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@IntegrationTest
public class TokenServiceTest {
  @Autowired
  private UserRepository userRepository;

  @Autowired
  private EntryRepository entryRepository;

  @Autowired
  private QueueRepository queueRepository;

  @Autowired
  private TokenRepository tokenRepository;

  @Autowired
  private TokenService tokenService;

  @AfterEach
  public void cleanAll() {
    userRepository.deleteAll();
    entryRepository.deleteAll();
    queueRepository.deleteAll();
    tokenRepository.deleteAll();
  }

  @Sql({"/users-create.sql", "/token-pairs-create.sql"})
  @Test
  @DisplayName("Should find token by access or refresh token")
  void findByValue() {
    var accessTokenValue = "access_one";
    var refreshTokenValue = "refresh_one";

    assertThat(tokenService.findByValue(accessTokenValue)).isPresent();
    assertThat(tokenService.findByValue(refreshTokenValue)).isPresent();
  }

  @Sql({"/users-create.sql", "/token-pairs-create.sql"})
  @Test
  @DisplayName("Is token valid should check the validity of token")
  void isTokenValid() {
    var validTokenPair = JpaRepositoryUtils.getById(1L, tokenRepository);
    var invalidTokenPair = JpaRepositoryUtils.getById(3L, tokenRepository);

    assertTrue(tokenService.isTokenValid(validTokenPair.getAccessToken()));
    assertTrue(tokenService.isTokenValid(validTokenPair.getRefreshToken()));
    assertFalse(tokenService.isTokenValid(invalidTokenPair.getRefreshToken()));
    assertFalse(tokenService.isTokenValid(invalidTokenPair.getRefreshToken()));
  }

  @Sql("/users-create.sql")
  @Test
  @DisplayName("Create should create new token")
  void create() {
    var user = JpaRepositoryUtils.getById(1L, userRepository);
    var accessTokenValue = "access.token";
    var refreshTokenValue = "refresh.token";
    var tokenPair = new TokenPair();
    tokenPair.setOwner(user);
    tokenPair.setAccessToken(accessTokenValue);
    tokenPair.setRefreshToken(refreshTokenValue);

    tokenService.create(tokenPair);
    assertThat(tokenService.findByValue(accessTokenValue)).isPresent();
  }

  @Sql({"/users-create.sql", "/token-pairs-create.sql"})
  @Test
  @DisplayName("Invalidate should invalidate token")
  void invalidate() {
    tokenService.invalidate("refresh_one");
    assertFalse(JpaRepositoryUtils.getById(1L, tokenRepository).isValid());
    assertTrue(JpaRepositoryUtils.getById(2L, tokenRepository).isValid());
  }

  @Sql({"/users-create.sql", "/token-pairs-create.sql"})
  @Test
  @DisplayName("Invalidate user tokens should invalidate all related token")
  void invalidateUserTokens() {
    tokenService.invalidateUserTokens(1L);
    assertFalse(JpaRepositoryUtils.getById(1L, tokenRepository).isValid());
    assertFalse(JpaRepositoryUtils.getById(2L, tokenRepository).isValid());
    assertTrue(JpaRepositoryUtils.getById(4L, tokenRepository).isValid());
  }
}
