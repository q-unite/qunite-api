package com.qunite.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.domain.User;
import java.time.Instant;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtService {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${spring.application.name}")
  private String issuer;

  public String createJwtToken(User user) {
    return JWT.create()
        .withSubject(user.getUsername())
        .withIssuer(issuer)
        .withIssuedAt(Instant.now())
        .sign(Algorithm.HMAC256(secret));
  }

  public Optional<DecodedJWT> verifyAccessToken(String token) {
    return Optional.of(JWT.require(Algorithm.HMAC256(secret))
        .withIssuer(issuer)
        .build()
        .verify(token));
  }
}
