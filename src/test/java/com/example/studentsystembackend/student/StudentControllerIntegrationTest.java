package com.example.studentsystembackend.student;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.studentsystembackend.auth.LoginRequest;
import com.example.studentsystembackend.auth.LoginResponse;
import com.example.studentsystembackend.auth.RegisterRequest;
import com.example.studentsystembackend.auth.Role;
import com.example.studentsystembackend.auth.AuthAccountRepository;
import com.example.studentsystembackend.auth.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class StudentControllerIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Autowired
  private StudentRepository studentRepository;

  @Autowired
  private AuthAccountRepository authAccountRepository;

  @Autowired
  private AuthService authService;

  @BeforeEach
  void setUp() {
    studentRepository.deleteAll();
    authAccountRepository.deleteAll();
    authService.seedDefaultAccounts();
  }

  @Test
  void shouldLoginAndReturnRole() throws Exception {
    LoginResponse loginResponse = login("admin", "admin123");

    assertThat(loginResponse.token()).isNotBlank();
    assertThat(loginResponse.role().name()).isEqualTo("ADMIN");
  }

  @Test
  void shouldRegisterStudentAccountAndReturnSession() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("newstudent");
    request.setPassword("pass1234");
    request.setRole(Role.STUDENT);

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.username").value("newstudent"))
      .andExpect(jsonPath("$.role").value("STUDENT"))
      .andExpect(jsonPath("$.token").isString());
  }

  @Test
  void shouldRegisterStudentAccountAndCreateStudentProfile() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("newstudent");
    request.setPassword("pass1234");
    request.setRole(Role.STUDENT);

    MvcResult result = mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn();

    LoginResponse response = objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);

    mockMvc.perform(get("/api/students/me")
        .header(HttpHeaders.AUTHORIZATION, bearerToken(response.token())))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.username").value("newstudent"))
      .andExpect(jsonPath("$.studentId").value(org.hamcrest.Matchers.startsWith("REG-")));
  }

  @Test
  void shouldRejectDuplicateUsernameOnRegister() throws Exception {
    RegisterRequest request = new RegisterRequest();
    request.setUsername("student");
    request.setPassword("pass1234");
    request.setRole(Role.STUDENT);

    mockMvc.perform(post("/api/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isConflict());
  }

  @Test
  void shouldAllowAdminToCreateStudent() throws Exception {
    String token = login("admin", "admin123").token();

    mockMvc.perform(post("/api/students")
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(student("createdstudent"))))
      .andExpect(status().isCreated())
      .andExpect(jsonPath("$.username").value("createdstudent"))
      .andExpect(jsonPath("$.grade").value("Pending"));
  }

  @Test
  void shouldRejectTeacherCreatingStudent() throws Exception {
    String token = login("teacher", "teacher123").token();

    mockMvc.perform(post("/api/students")
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(student("createdstudent"))))
      .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowTeacherToViewAllStudents() throws Exception {
    String token = login("teacher", "teacher123").token();
    studentRepository.save(student("learner2"));

    mockMvc.perform(get("/api/students")
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.length()").value(2));
  }

  @Test
  void shouldRejectStudentFromViewingAllStudents() throws Exception {
    String token = login("student", "student123").token();

    mockMvc.perform(get("/api/students")
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
      .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowStudentToViewOwnProfileOnly() throws Exception {
    String token = login("student", "student123").token();
    Student ownStudent = studentRepository.findByUsername("student").orElseThrow();
    studentRepository.save(student("learner2"));

    mockMvc.perform(get("/api/students/me")
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.id").value(ownStudent.getId()))
      .andExpect(jsonPath("$.username").value("student"));
  }

  @Test
  void shouldRejectStudentViewingAnotherStudentById() throws Exception {
    String token = login("student", "student123").token();
    Student otherStudent = studentRepository.save(student("learner2"));

    mockMvc.perform(get("/api/students/{id}", otherStudent.getId())
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
      .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowStudentToUpdateOwnProfile() throws Exception {
    String token = login("student", "student123").token();
    StudentProfileUpdateRequest request = new StudentProfileUpdateRequest();
    request.setName("Student Updated");
    request.setCourse("Software Engineering");
    request.setStudentClass("CS-B");
    request.setYear("Year 3");
    request.setEnrolledCourses("Algorithms, Operating Systems");

    mockMvc.perform(put("/api/students/me")
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.name").value("Student Updated"))
      .andExpect(jsonPath("$.course").value("Software Engineering"))
      .andExpect(jsonPath("$.enrolledCourses").value("Algorithms, Operating Systems"));
  }

  @Test
  void shouldAllowTeacherToUpdateAcademicRecord() throws Exception {
    String token = login("teacher", "teacher123").token();
    Student savedStudent = studentRepository.save(student("learner1"));
    StudentAcademicUpdateRequest request = new StudentAcademicUpdateRequest();
    request.setEnrolledCourses("OOP, Databases, Networking");
    request.setGrade("A");
    request.setAttendancePercentage(96);

    mockMvc.perform(put("/api/students/{id}/academic", savedStudent.getId())
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.grade").value("A"))
      .andExpect(jsonPath("$.attendancePercentage").value(96));
  }

  @Test
  void shouldRejectStudentUpdatingAcademicRecord() throws Exception {
    String token = login("student", "student123").token();
    Student savedStudent = studentRepository.save(student("learner1"));
    StudentAcademicUpdateRequest request = new StudentAcademicUpdateRequest();
    request.setEnrolledCourses("OOP");
    request.setGrade("A");
    request.setAttendancePercentage(90);

    mockMvc.perform(put("/api/students/{id}/academic", savedStudent.getId())
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isForbidden());
  }

  @Test
  void shouldAllowAdminToUpdateStudent() throws Exception {
    String token = login("admin", "admin123").token();
    Student savedStudent = studentRepository.save(student("learner1"));
    Student updatedStudent = student("learner1");
    updatedStudent.setCourse("Software Engineering");
    updatedStudent.setYear("Year 3");
    updatedStudent.setStatus("Graduated");

    mockMvc.perform(put("/api/students/{id}", savedStudent.getId())
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updatedStudent)))
      .andExpect(status().isOk())
      .andExpect(jsonPath("$.course").value("Software Engineering"))
      .andExpect(jsonPath("$.year").value("Year 3"))
      .andExpect(jsonPath("$.status").value("Graduated"));
  }

  @Test
  void shouldAllowAdminToDeleteStudent() throws Exception {
    String token = login("admin", "admin123").token();
    Student savedStudent = studentRepository.save(student("learner1"));

    mockMvc.perform(delete("/api/students/{id}", savedStudent.getId())
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
      .andExpect(status().isNoContent());
  }

  @Test
  void shouldRejectTeacherDeletingStudent() throws Exception {
    String token = login("teacher", "teacher123").token();
    Student savedStudent = studentRepository.save(student("learner1"));

    mockMvc.perform(delete("/api/students/{id}", savedStudent.getId())
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token)))
      .andExpect(status().isForbidden());
  }

  @Test
  void shouldRejectDuplicateStudentId() throws Exception {
    String token = login("admin", "admin123").token();
    Student existingStudent = studentRepository.save(student("learner1"));
    Student duplicateStudent = student("learner2");
    duplicateStudent.setStudentId(existingStudent.getStudentId());

    mockMvc.perform(post("/api/students")
        .header(HttpHeaders.AUTHORIZATION, bearerToken(token))
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(duplicateStudent)))
      .andExpect(status().isConflict());
  }

  private Student student(String username) {
    return new Student(
      null,
      "Amina Yusuf",
      "ST-" + username.toUpperCase(),
      username,
      "Computer Science",
      "CS-A",
      "Year 2",
      "Active",
      "OOP, Databases",
      "Pending",
      85
    );
  }

  private LoginResponse login(String username, String password) throws Exception {
    LoginRequest request = new LoginRequest();
    request.setUsername(username);
    request.setPassword(password);

    MvcResult result = mockMvc.perform(post("/api/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
      .andExpect(status().isOk())
      .andReturn();

    return objectMapper.readValue(result.getResponse().getContentAsString(), LoginResponse.class);
  }

  private String bearerToken(String token) {
    return "Bearer " + token;
  }
}
