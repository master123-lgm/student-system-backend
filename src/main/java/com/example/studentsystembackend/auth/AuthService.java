package com.example.studentsystembackend.auth;

import com.example.studentsystembackend.student.Student;
import com.example.studentsystembackend.student.StudentRepository;
import jakarta.annotation.PostConstruct;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

  private static final String DEFAULT_ENROLLED_COURSES = "Not assigned yet";
  private static final String DEFAULT_GRADE = "Pending";
  private static final String DEFAULT_STATUS = "Active";
  private static final String DEFAULT_COURSE = "Not assigned";
  private static final String DEFAULT_CLASS = "Unassigned";
  private static final String DEFAULT_YEAR = "Year 1";

  private final AuthAccountRepository authAccountRepository;
  private final StudentRepository studentRepository;
  private final PasswordEncoder passwordEncoder;
  private final Map<String, AuthUser> activeSessions = new ConcurrentHashMap<>();

  public AuthService(
    AuthAccountRepository authAccountRepository,
    StudentRepository studentRepository,
    PasswordEncoder passwordEncoder
  ) {
    this.authAccountRepository = authAccountRepository;
    this.studentRepository = studentRepository;
    this.passwordEncoder = passwordEncoder;
  }

  @PostConstruct
  public void seedDefaultAccounts() {
    seedAccount("admin", "admin123", Role.ADMIN);
    seedAccount("teacher", "teacher123", Role.TEACHER);
    seedAccount("student", "student123", Role.STUDENT);
    ensureStudentProfileExists("student");
  }

  public LoginResponse login(LoginRequest request) {
    String username = normalizeUsername(request.getUsername());
    AuthAccount account = authAccountRepository.findByUsername(username)
      .orElse(null);

    if (account == null || !passwordEncoder.matches(request.getPassword().trim(), account.getPasswordHash())) {
      throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
    }

    return createSession(account.getUsername(), account.getRole());
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

    if (authAccountRepository.existsByUsername(username)) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }

    authAccountRepository.save(new AuthAccount(null, username, passwordEncoder.encode(password), role));

    if (role == Role.STUDENT) {
      ensureStudentProfileExists(username);
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

  private void seedAccount(String username, String password, Role role) {
    if (authAccountRepository.existsByUsername(username)) {
      return;
    }

    authAccountRepository.save(new AuthAccount(null, username, passwordEncoder.encode(password), role));
  }

  private void ensureStudentProfileExists(String username) {
    if (studentRepository.existsByUsername(username)) {
      return;
    }

    Student student = new Student();
    student.setName(buildDefaultStudentName(username));
    student.setStudentId(generateStudentId(username));
    student.setUsername(username);
    student.setCourse(DEFAULT_COURSE);
    student.setStudentClass(DEFAULT_CLASS);
    student.setYear(DEFAULT_YEAR);
    student.setStatus(DEFAULT_STATUS);
    student.setEnrolledCourses(DEFAULT_ENROLLED_COURSES);
    student.setGrade(DEFAULT_GRADE);
    student.setAttendancePercentage(0);

    studentRepository.save(student);
  }

  private String buildDefaultStudentName(String username) {
    if (username.isBlank()) {
      return "New Student";
    }

    String firstCharacter = username.substring(0, 1).toUpperCase(Locale.ROOT);
    String remainder = username.substring(1);
    return firstCharacter + remainder;
  }

  private String generateStudentId(String username) {
    String compactUsername = username.replaceAll("[^a-z0-9]", "").toUpperCase(Locale.ROOT);
    String suffix = compactUsername.isBlank() ? "USER" : compactUsername;
    suffix = suffix.length() > 8 ? suffix.substring(0, 8) : suffix;
    return "REG-" + suffix + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
  }
}
