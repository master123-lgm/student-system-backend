package com.example.studentsystembackend.student;

import com.example.studentsystembackend.auth.AuthService;
import com.example.studentsystembackend.auth.AuthUser;
import com.example.studentsystembackend.auth.Role;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class StudentService {

  private final AuthService authService;
  private final StudentRepository studentRepository;

  public StudentService(AuthService authService, StudentRepository studentRepository) {
    this.authService = authService;
    this.studentRepository = studentRepository;
  }

  public Student createStudent(Student student, AuthUser user) {
    authService.requireAdmin(user);
    normalizeStudent(student);
    validateUniqueStudentId(student.getStudentId(), null);
    validateUniqueUsername(student.getUsername(), null);
    return studentRepository.save(student);
  }

  public List<Student> getAllStudents(String searchTerm, String studentClass, String year, String status, AuthUser user) {
    authService.requireTeacherOrAdmin(user);
    return studentRepository.searchStudents(
      normalizeFilter(searchTerm),
      normalizeFilter(studentClass),
      normalizeFilter(year),
      normalizeFilter(status)
    );
  }

  public Student getStudentById(Long id, AuthUser user) {
    Student student = studentRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

    if (user.role() == Role.STUDENT
      && !student.getUsername().equals(user.username())) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Students can only view their own profile");
    }

    return student;
  }

  public Student getOwnProfile(AuthUser user) {
    authService.requireStudent(user);
    return studentRepository.findByUsername(user.username())
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student profile not found"));
  }

  public Student updateOwnProfile(StudentProfileUpdateRequest request, AuthUser user) {
    authService.requireStudent(user);
    Student existingStudent = getOwnProfile(user);

    existingStudent.setName(request.getName().trim());
    existingStudent.setCourse(request.getCourse().trim());
    existingStudent.setStudentClass(request.getStudentClass().trim());
    existingStudent.setYear(request.getYear().trim());
    existingStudent.setEnrolledCourses(request.getEnrolledCourses().trim());

    return studentRepository.save(existingStudent);
  }

  public Student updateStudent(Long id, Student updatedStudent, AuthUser user) {
    authService.requireAdmin(user);
    normalizeStudent(updatedStudent);
    Student existingStudent = getStudentById(id, user);
    validateUniqueStudentId(updatedStudent.getStudentId(), id);
    validateUniqueUsername(updatedStudent.getUsername(), id);

    existingStudent.setName(updatedStudent.getName());
    existingStudent.setStudentId(updatedStudent.getStudentId());
    existingStudent.setUsername(updatedStudent.getUsername());
    existingStudent.setCourse(updatedStudent.getCourse());
    existingStudent.setStudentClass(updatedStudent.getStudentClass());
    existingStudent.setYear(updatedStudent.getYear());
    existingStudent.setStatus(updatedStudent.getStatus());
    existingStudent.setEnrolledCourses(updatedStudent.getEnrolledCourses());
    existingStudent.setGrade(updatedStudent.getGrade());
    existingStudent.setAttendancePercentage(updatedStudent.getAttendancePercentage());

    return studentRepository.save(existingStudent);
  }

  public Student updateAcademicRecord(Long id, StudentAcademicUpdateRequest request, AuthUser user) {
    authService.requireTeacherOrAdmin(user);
    Student existingStudent = studentRepository.findById(id)
      .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Student not found"));

    existingStudent.setEnrolledCourses(request.getEnrolledCourses().trim());
    existingStudent.setGrade(request.getGrade().trim());
    existingStudent.setAttendancePercentage(request.getAttendancePercentage());

    return studentRepository.save(existingStudent);
  }

  public void deleteStudent(Long id, AuthUser user) {
    authService.requireAdmin(user);
    Student existingStudent = getStudentById(id, user);
    studentRepository.delete(existingStudent);
  }

  private void validateUniqueStudentId(String studentId, Long currentId) {
    boolean exists = currentId == null
      ? studentRepository.existsByStudentId(studentId)
      : studentRepository.existsByStudentIdAndIdNot(studentId, currentId);

    if (exists) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Student ID already exists");
    }
  }

  private void validateUniqueUsername(String username, Long currentId) {
    boolean exists = currentId == null
      ? studentRepository.existsByUsername(username)
      : studentRepository.existsByUsernameAndIdNot(username, currentId);

    if (exists) {
      throw new ResponseStatusException(HttpStatus.CONFLICT, "Username already exists");
    }
  }

  private void normalizeStudent(Student student) {
    student.setName(student.getName().trim());
    student.setStudentId(student.getStudentId().trim());
    student.setUsername(student.getUsername().trim().toLowerCase(Locale.ROOT));
    student.setCourse(student.getCourse().trim());
    student.setStudentClass(student.getStudentClass().trim());
    student.setYear(student.getYear().trim());
    student.setStatus(student.getStatus().trim());
    student.setEnrolledCourses(student.getEnrolledCourses().trim());
    student.setGrade(student.getGrade().trim());
    student.setAttendancePercentage(student.getAttendancePercentage());
  }

  private String normalizeFilter(String value) {
    if (value == null) {
      return null;
    }

    String trimmedValue = value.trim();
    return trimmedValue.isEmpty() ? null : trimmedValue.toLowerCase(Locale.ROOT);
  }
}
