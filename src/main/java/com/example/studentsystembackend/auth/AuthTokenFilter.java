package com.example.studentsystembackend.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthTokenFilter extends OncePerRequestFilter {

  private final AuthService authService;

  public AuthTokenFilter(AuthService authService) {
    this.authService = authService;
  }

  @Override
  protected boolean shouldNotFilter(HttpServletRequest request) {
    String path = request.getRequestURI();
    return "OPTIONS".equalsIgnoreCase(request.getMethod())
      || !path.startsWith("/api/students")
      || path.startsWith("/h2-console");
  }

  @Override
  protected void doFilterInternal(
    HttpServletRequest request,
    HttpServletResponse response,
    FilterChain filterChain
  ) throws ServletException, IOException {
    String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
      response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing authorization token");
      return;
    }

    String token = authorizationHeader.substring(7);
    AuthUser user = authService.authenticate(token)
      .orElse(null);

    if (user == null) {
      response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid authorization token");
      return;
    }

    request.setAttribute(AuthUser.REQUEST_ATTRIBUTE, user);
    filterChain.doFilter(request, response);
  }
}
