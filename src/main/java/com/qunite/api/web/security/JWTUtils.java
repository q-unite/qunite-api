package com.qunite.api.web.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.domain.User;
import java.util.Date;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class JWTUtils {
  Algorithm algorithm;
  public DecodedJWT verify(String token) {
    JWTVerifier jwtVerifier = JWT.require(algorithm).build();
    return jwtVerifier.verify(token);
  }
  public String create(String username, String issuer){
  return JWT.create().withSubject(username).withExpiresAt(new Date(Long.MAX_VALUE))
        .withIssuer(issuer)
        .sign(algorithm);
  }
}
