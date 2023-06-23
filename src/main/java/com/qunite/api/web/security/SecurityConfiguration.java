package com.qunite.api.web.security;

import com.auth0.jwt.algorithms.Algorithm;
import com.qunite.api.web.security.filters.JwtTokenFilter;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfiguration {
  JwtTokenFilter jwtTokenFilter;
  @Bean
  public SecurityFilterChain getChain(HttpSecurity httpSecurity) throws Exception {
    httpSecurity.csrf().disable().cors()
        .and().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
        .and()
        .addFilterBefore(jwtTokenFilter, UsernamePasswordAuthenticationFilter.class);

    return httpSecurity.authorizeHttpRequests(requests -> {
      requests
          .requestMatchers(HttpMethod.POST, "/auth/*").permitAll()
          .requestMatchers(HttpMethod.POST, "/users").permitAll()
          .requestMatchers("/swagger-ui.html").permitAll()
          .anyRequest().authenticated();
    }).build();
  }

  @Bean
  public PasswordEncoder getEncoder() {
    return new BCryptPasswordEncoder();
  }


  @Bean
  AuthenticationManager authenticationManager(
      AuthenticationConfiguration authenticationConfiguration)
      throws Exception {
    return authenticationConfiguration.getAuthenticationManager();
  }
}
