package com.qunite.api.web.security;

import com.auth0.jwt.algorithms.Algorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
  @Bean
  public SecurityFilterChain getChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.csrf().disable().cors()
        .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

    return httpSecurity.authorizeHttpRequests(requests -> {
      requests
        .requestMatchers("/auth/*").permitAll()
          .anyRequest().permitAll();
    }).build();
  }

  @Value("${security.secret}")
  String secret;

  @Bean
  public PasswordEncoder getEncoder() {
    return new BCryptPasswordEncoder();
  }

  @Bean
  public Algorithm getAlgorithm() {
    return Algorithm.HMAC256(secret);
  }
}
