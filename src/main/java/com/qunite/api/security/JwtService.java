package com.qunite.api.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.data.UserRepository;
import com.qunite.api.domain.Tokens;
import com.qunite.api.domain.User;
import com.qunite.api.service.TokenService;
import com.qunite.api.web.dto.auth.AuthenticationResponse;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class JwtService {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${spring.application.name}")
  private String issuer;

  private final TokenService tokenService;

  private final UserRepository userRepository;

  @Transactional
  public AuthenticationResponse createJwtTokens(User user) {
    String accessToken = generateJwtToken(
        user, 30, ChronoUnit.MINUTES, TokenType.ACCESS_TOKEN);
    String refreshToken = generateJwtToken(
        user, 7, ChronoUnit.DAYS, TokenType.REFRESH_TOKEN);

    Tokens tokens = new Tokens();
    tokens.setAccessToken(accessToken);
    tokens.setRefreshToken(refreshToken);
    tokens.setOwner(user);
    tokenService.createOne(tokens);

    return new AuthenticationResponse(accessToken, refreshToken, "JWT", "HS256", 30 * 60);
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
        .withClaim("passwordHash", user.getPassword().hashCode())
        .sign(Algorithm.HMAC256(secret));
  }

  @Transactional
  public Optional<DecodedJWT> verifyToken(String token, TokenType tokenType) {
    return Optional.of(JWT.require(Algorithm.HMAC256(secret))
            .withIssuer(issuer)
            .build()
            .verify(token))
        .filter(decodedJWT -> tokenService.isTokenValid(decodedJWT.getToken()))
        .filter(this::isDataValid)
        .filter(decodedJWT -> decodedJWT.getClaim("type").asString()
            .equals(tokenType.getValue()));
  }

  private boolean isDataValid(DecodedJWT decodedJWT) {
    return userRepository.findById(Long.valueOf(decodedJWT.getSubject()))
        .filter(found -> found.getUsername().equals(decodedJWT.getClaim("username").asString()))
        .filter(found -> found.getPassword().hashCode() ==
            (decodedJWT.getClaim("passwordHash").asInt()))
        .isPresent();
  }
}