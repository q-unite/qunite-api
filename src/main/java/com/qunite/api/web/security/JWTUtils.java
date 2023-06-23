package com.qunite.api.web.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import java.util.Date;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class JWTUtils {
  //todo put in env\properties\etc
  String secret = "secret";
  //todo had problems with "beaning" it
  Algorithm algorithm = Algorithm.HMAC256(secret);


  public DecodedJWT verify(String token) {
    JWTVerifier jwtVerifier = JWT.require(algorithm).build();
    return jwtVerifier.verify(token);
  }
  public String create(String username, String issuer){
  return JWT.create()
      .withSubject(username).withExpiresAt(new Date(Long.MAX_VALUE))
      .withIssuer(issuer)
        .sign(algorithm);
  }
}
