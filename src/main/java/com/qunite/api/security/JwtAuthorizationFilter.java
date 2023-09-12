package com.qunite.api.security;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Component
@SecurityScheme(name = "bearer_token", type = SecuritySchemeType.HTTP, scheme = "bearer",
    description = "Enter the token given after successful POST /auth/sign-in",
    bearerFormat = "JWT")
public class JwtAuthorizationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;


  private final HandlerExceptionResolver resolver;

  public JwtAuthorizationFilter(JwtService jwtService,
                                @Qualifier("handlerExceptionResolver")
                                HandlerExceptionResolver resolver) {
    this.jwtService = jwtService;
    this.resolver = resolver;
  }

  @Override
  protected void doFilterInternal(@NonNull HttpServletRequest request,
                                  @NonNull HttpServletResponse response,
                                  @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    var authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
    if (!this.isHeaderValid(authHeader)) {
      filterChain.doFilter(request, response);
      return;
    }
    try {
      jwtService.verifyJwt(authHeader.substring(7), TokenType.ACCESS)
          .ifPresent(decodedJWT -> {
            var username = decodedJWT.getClaim("username").asString();
            var authToken = new UsernamePasswordAuthenticationToken(
                username, null, Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
          });
    } catch (RuntimeException exception) {
      resolver.resolveException(request, response, null, exception);
      return;
    }

    filterChain.doFilter(request, response);
  }

  private boolean isHeaderValid(String authHeader) {
    return authHeader != null && authHeader.startsWith("Bearer ");
  }
}

