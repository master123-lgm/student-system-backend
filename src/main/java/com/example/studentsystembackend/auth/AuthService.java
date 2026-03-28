package com.example.studentsystembackend.auth;

import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private final Map<String, UserCredentials> users = new ConcurrentHashMap<>();
  private final Map<String, AuthUser> activeSessions = new ConcurrentHashMap<>();

  public AuthService() {
    users.put("admin", new UserCredentials("admin123", Role.ADMIN));
    users.put("teacher", new UserCredentials("teacher123", Role.TEACHER));
    users.put("student", new UserCredentials("student123", Role.STUDENT));
  }

  public LoginResponse login(LoginRequest request) {
    String username = normalizeUsername(request.getUsername());
    UserCredentials credentials = users.get(username);

    if (credentials == null || !credentials.password().equals(request.getPassword().trim())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    return createSession(username, credentials.role());
  }

  public LoginResponse register(RegisterRequest request) {
    String username = normalizeUsername(request.getUsername());
    String password = request.getPassword().trim();
    Role role = request.getRole();

    if (role == Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin accounts cannot be created from the sign up page");
    }

    if (password.length() < 4) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Password must be at least 4 characters");
    }

    UserCredentials existingUser = users.putIfAbsent(username, new UserCredentials(password, role));

    if (existingUser != null) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }

    return createSession(username, role);
  }

  public Optional<AuthUser> authenticate(String token) {
    return Optional.ofNullable(activeSessions.get(token));
  }

  public void requireAdmin(AuthUser user) {
    if (user.role() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only admins can update or delete records");
    }
  }

  public void requireTeacherOrAdmin(AuthUser user) {
    if (user.role() != Role.TEACHER && user.role() != Role.ADMIN) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only teachers or admins can manage academic records");
    }
  }

  public void requireStudent(AuthUser user) {
    if (user.role() != Role.STUDENT) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "This action is available to students only");
    }
  }

  private LoginResponse createSession(String username, Role role) {
    String token = UUID.randomUUID().toString();
    AuthUser user = new AuthUser(username, role);
    activeSessions.put(token, user);

    return new LoginResponse(token, user.username(), user.role());
  }

  private String normalizeUsername(String username) {
    String normalizedUsername = username.trim().toLowerCase(Locale.ROOT);

    if (normalizedUsername.isBlank()) {
      throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username is required");
    }

    return normalizedUsername;
  }

  private record UserCredentials(String password, Role role) {
  }
}
