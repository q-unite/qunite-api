package com.qunite.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.domain.User;
import com.qunite.api.service.UserService;
import java.time.Instant;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor(onConstructor_ = {@Lazy})
public class JwtService {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${spring.application.name}")
  private String issuer;

  private final UserService userService;

  public String createJwtToken(User user) {
    return JWT.create()
        .withSubject(String.valueOf(user.getId()))
        .withIssuer(issuer)
        .withClaim("passwordHash", user.getPassword())
        .withClaim("username", user.getUsername())
        .withIssuedAt(Instant.now())
        .sign(Algorithm.HMAC256(secret));
  }

  public Optional<DecodedJWT> verifyAccessToken(String token) {
    return Optional.of(JWT.require(Algorithm.HMAC256(secret))
            .withIssuer(issuer)
            .build()
            .verify(token))
        .filter(this::isDataValid);
  }

  private boolean isDataValid(DecodedJWT decodedJWT) {
    return userService.findOne(Long.valueOf(decodedJWT.getSubject()))
        .filter(found -> found.getUsername().equals(decodedJWT.getClaim("username").asString()))
        .filter(
            found -> found.getPassword().hashCode() ==
                (decodedJWT.getClaim("passwordHash").asInt()))
        .isPresent();
  }
}
