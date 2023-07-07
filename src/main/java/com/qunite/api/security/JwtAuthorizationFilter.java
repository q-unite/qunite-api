package com.qunite.api.security;

import com.auth0.jwt.exceptions.JWTDecodeException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@RequiredArgsConstructor
@Component
public class JwtAuthorizationFilter extends OncePerRequestFilter {
  private final JwtService jwtService;

  @Autowired
  @Qualifier("handlerExceptionResolver")
  private HandlerExceptionResolver resolver;

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
      jwtService.verifyAccessToken(authHeader.substring(7))
          .ifPresent(decodedJWT -> {
            var username = decodedJWT.getSubject();
            var authToken = new UsernamePasswordAuthenticationToken(
                username, null, Collections.emptyList()
            );
            SecurityContextHolder.getContext().setAuthentication(authToken);
          });
      filterChain.doFilter(request, response);
    } catch (JWTDecodeException exception) {
      resolver.resolveException(request, response, null, exception);
    }
  }

  private boolean isHeaderValid(String authHeader) {
    return authHeader != null && authHeader.startsWith("Bearer ");
  }
}

