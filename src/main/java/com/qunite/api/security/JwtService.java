package com.qunite.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Tokens;
import com.qunite.api.domain.User;
import com.qunite.api.service.TokenService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class JwtService {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${spring.application.name}")
  private String issuer;

  private final TokenService tokenService;

  private final UserRepository userRepository;

  @Value("${jwt.access-token-expiration-time}")
  private Integer expirationTime;

  public Tokens createJwtTokens(User user) {
    String accessToken = generateJwtToken(
        user, expirationTime, ChronoUnit.SECONDS, TokenType.ACCESS);
    String refreshToken = generateJwtToken(
        user, 7, ChronoUnit.DAYS, TokenType.REFRESH);

    Tokens tokens = new Tokens();
    tokens.setAccessToken(accessToken);
    tokens.setRefreshToken(refreshToken);
    tokens.setOwner(user);
    return tokenService.create(tokens);
  }

  public Optional<DecodedJWT> verifyToken(String token, TokenType tokenType) {
    return Optional.of(JWT.require(Algorithm.HMAC256(secret))
            .withIssuer(issuer)
            .build()
            .verify(token))
        .filter(decodedJWT -> tokenService.isTokenValid(decodedJWT.getToken()))
        .filter(decodedJWT -> decodedJWT.getClaim("type").asString()
            .equals(tokenType.getValue()))
        .filter(this::isDataValid);
  }

  private String generateJwtToken(User user, int expirationTime, ChronoUnit expirationTimeUnit,
                                  TokenType type) {
    return JWT.create()
        .withSubject(String.valueOf(user.getId()))
        .withIssuer(issuer)
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plus(expirationTime, expirationTimeUnit))
        .withClaim("type", type.getValue())
        .withClaim("username", user.getUsername())
        .withClaim("password", user.getPassword())
        .sign(Algorithm.HMAC256(secret));
  }

  private boolean isDataValid(DecodedJWT decodedJWT) {
    return userRepository.findById(Long.valueOf(decodedJWT.getSubject()))
        .filter(found -> found.getUsername().equals(decodedJWT.getClaim("username").asString()))
        .filter(found -> found.getPassword().equals(decodedJWT.getClaim("password").asString()))
        .isPresent();
  }
}