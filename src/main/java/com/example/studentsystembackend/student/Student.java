package com.example.studentsystembackend.student;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

@Entity
@Table(name = "students")
public class Student {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @NotBlank
  @Column(nullable = false)
  private String name;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String studentId;

  @NotBlank
  @Column(nullable = false, unique = true)
  private String username;

  @NotBlank
  @Column(nullable = false)
  private String course;

  @NotBlank
  @Column(nullable = false)
  private String studentClass;

  @NotBlank
  @Column(name = "study_year", nullable = false)
  private String year;

  @NotBlank
  @Column(nullable = false)
  private String status;

  @NotBlank
  @Column(nullable = false, length = 1000)
  private String enrolledCourses;

  @NotBlank
  @Column(nullable = false)
  private String grade;

  @Column(nullable = false)
  private Integer attendancePercentage;

  public Student() {
  }

  public Student(
    Long id,
    String name,
    String studentId,
    String username,
    String course,
    String studentClass,
    String year,
    String status,
    String enrolledCourses,
    String grade,
    Integer attendancePercentage
  ) {
    this.id = id;
    this.name = name;
    this.studentId = studentId;
    this.username = username;
    this.course = course;
    this.studentClass = studentClass;
    this.year = year;
    this.status = status;
    this.enrolledCourses = enrolledCourses;
    this.grade = grade;
    this.attendancePercentage = attendancePercentage;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getStudentId() {
    return studentId;
  }

  public void setStudentId(String studentId) {
    this.studentId = studentId;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getCourse() {
    return course;
  }

  public void setCourse(String course) {
    this.course = course;
  }

  public String getStudentClass() {
    return studentClass;
  }

  public void setStudentClass(String studentClass) {
    this.studentClass = studentClass;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getStatus() {
    return status;
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getEnrolledCourses() {
    return enrolledCourses;
  }

  public void setEnrolledCourses(String enrolledCourses) {
    this.enrolledCourses = enrolledCourses;
  }

  public String getGrade() {
    return grade;
  }

  public void setGrade(String grade) {
    this.grade = grade;
  }

  public Integer getAttendancePercentage() {
    return attendancePercentage;
  }

  public void setAttendancePercentage(Integer attendancePercentage) {
    this.attendancePercentage = attendancePercentage;
  }
}
