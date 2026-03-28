package com.example.studentsystembackend.student;

import jakarta.validation.constraints.NotBlank;

public class StudentProfileUpdateRequest {

  @NotBlank
  private String name;

  @NotBlank
  private String course;

  @NotBlank
  private String studentClass;

  @NotBlank
  private String year;

  @NotBlank
  private String enrolledCourses;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
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

  public String getEnrolledCourses() {
    return enrolledCourses;
  }

  public void setEnrolledCourses(String enrolledCourses) {
    this.enrolledCourses = enrolledCourses;
  }
}
