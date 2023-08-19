package com.qunite.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.domain.TokenPair;
import com.qunite.api.domain.User;
import com.qunite.api.service.TokenService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;
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

  @Value("${jwt.access-token-expiration-time}")
  private Integer expirationTime;

  private final TokenService tokenService;

  public TokenPair createJwt(User user) {
    String accessToken = generateJwt(
        user, expirationTime, ChronoUnit.SECONDS, TokenType.ACCESS);
    String refreshToken = generateJwt(
        user, 7, ChronoUnit.DAYS, TokenType.REFRESH);

    TokenPair tokenPair = new TokenPair();
    tokenPair.setAccessToken(accessToken);
    tokenPair.setRefreshToken(refreshToken);
    tokenPair.setOwner(user);
    return tokenService.create(tokenPair);
  }

  public Optional<DecodedJWT> verifyJwt(String token, TokenType tokenType) {
    return Optional.of(JWT.require(Algorithm.HMAC256(secret))
            .withIssuer(issuer)
            .withClaim("type", tokenType.getValue())
            .build()
            .verify(token))
        .filter(decodedJWT -> tokenService.isTokenValid(decodedJWT.getToken()));
  }

  private String generateJwt(User user, int expirationTime, ChronoUnit expirationTimeUnit,
                             TokenType type) {
    return JWT.create()
        .withSubject(user.getId().toString())
        .withIssuer(issuer)
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plus(expirationTime, expirationTimeUnit))
        .withClaim("type", type.getValue())
        .withClaim("username", user.getUsername())
        .withJWTId(UUID.randomUUID().toString())
        .sign(Algorithm.HMAC256(secret));
  }
}