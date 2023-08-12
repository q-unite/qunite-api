package com.qunite.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.domain.AccessToken;
import com.qunite.api.domain.User;
import com.qunite.api.service.TokenService;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtService {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${spring.application.name}")
  private String issuer;

  private final TokenService tokenService;

  public String createJwtToken(User user) {
    String tokenValue = JWT.create()
        .withSubject(user.getUsername())
        .withIssuer(issuer)
        .withIssuedAt(Instant.now())
        .withExpiresAt(Instant.now().plus(10, ChronoUnit.MINUTES))
        .sign(Algorithm.HMAC256(secret));

    AccessToken token = new AccessToken();
    token.setValue(tokenValue);
    user.addAccessToken(token);
    return tokenValue;
  }

  public Optional<DecodedJWT> verifyAccessToken(String token) {
    return Optional.of(JWT.require(Algorithm.HMAC256(secret))
            .withIssuer(issuer)
            .build()
            .verify(token))
        .filter(decodedJWT -> tokenService.isTokenValid(token));
  }
}
