package com.qunite.api.web.security.filters;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.qunite.api.web.security.JWTUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@AllArgsConstructor
@Component
public class JwtTokenFilter extends OncePerRequestFilter {


  JWTUtils jwtUtils;
  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
    final String header = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (Objects.isNull(header) || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }
    String token = header.substring(7).trim();
    DecodedJWT decodedJWT = jwtUtils.verify(token);
    String username = decodedJWT.getSubject();

    UsernamePasswordAuthenticationToken
        authenticationToken = new UsernamePasswordAuthenticationToken(username, null, List.of(new SimpleGrantedAuthority("user")));
    SecurityContextHolder.getContext().setAuthentication(authenticationToken);
    filterChain.doFilter(request, response);

  }
}


