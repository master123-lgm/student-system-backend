package com.example.studentsystembackend.student;

import com.example.studentsystembackend.auth.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/students")
public class StudentController {

  private final StudentService studentService;

  public StudentController(StudentService studentService) {
    this.studentService = studentService;
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public Student createStudent(@Valid @RequestBody Student student, HttpServletRequest request) {
    AuthUser user = (AuthUser) request.getAttribute(AuthUser.REQUEST_ATTRIBUTE);
    return studentService.createStudent(student, user);
  }

  @GetMapping
  public List<Student> getAllStudents(
    @RequestParam(required = false) String search,
    @RequestParam(required = false) String studentClass,
    @RequestParam(required = false) String year,
    @RequestParam(required = false) String status,
    HttpServletRequest request
  ) {
    AuthUser user = (AuthUser) request.getAttribute(AuthUser.REQUEST_ATTRIBUTE);
    return studentService.getAllStudents(search, studentClass, year, status, user);
  }

  @GetMapping("/me")
  public Student getOwnProfile(HttpServletRequest request) {
    AuthUser user = (AuthUser) request.getAttribute(AuthUser.REQUEST_ATTRIBUTE);
    return studentService.getOwnProfile(user);
  }

  @GetMapping("/{id}")
  public Student getStudentById(@PathVariable Long id, HttpServletRequest request) {
    AuthUser user = (AuthUser) request.getAttribute(AuthUser.REQUEST_ATTRIBUTE);
    return studentService.getStudentById(id, user);
  }

  @PutMapping("/me")
  public Student updateOwnProfile(
    @Valid @RequestBody StudentProfileUpdateRequest requestBody,
    HttpServletRequest request
  ) {
    AuthUser user = (AuthUser) request.getAttribute(AuthUser.REQUEST_ATTRIBUTE);
    return studentService.updateOwnProfile(requestBody, user);
  }

  @PutMapping("/{id}")
  public Student updateStudent(
    @PathVariable Long id,
    @Valid @RequestBody Student student,
    HttpServletRequest request
  ) {
    AuthUser user = (AuthUser) request.getAttribute(AuthUser.REQUEST_ATTRIBUTE);
    return studentService.updateStudent(id, student, user);
  }

  @PutMapping("/{id}/academic")
  public Student updateAcademicRecord(
    @PathVariable Long id,
    @Valid @RequestBody StudentAcademicUpdateRequest requestBody,
    HttpServletRequest request
  ) {
    AuthUser user = (AuthUser) request.getAttribute(AuthUser.REQUEST_ATTRIBUTE);
    return studentService.updateAcademicRecord(id, requestBody, user);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteStudent(@PathVariable Long id, HttpServletRequest request) {
    AuthUser user = (AuthUser) request.getAttribute(AuthUser.REQUEST_ATTRIBUTE);
    studentService.deleteStudent(id, user);
  }
}
