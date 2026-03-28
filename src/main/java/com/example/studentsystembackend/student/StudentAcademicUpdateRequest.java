package com.example.studentsystembackend.student;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class StudentAcademicUpdateRequest {

  @NotBlank
  private String enrolledCourses;

  @NotBlank
  private String grade;

  @Min(0)
  @Max(100)
  private Integer attendancePercentage;

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
