package com.example.studentsystembackend.student;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudentRepository extends JpaRepository<Student, Long> {

  boolean existsByStudentIdAndIdNot(String studentId, Long id);

  boolean existsByStudentId(String studentId);

  boolean existsByUsername(String username);

  boolean existsByUsernameAndIdNot(String username, Long id);

  java.util.Optional<Student> findByUsername(String username);

  @Query("""
    select student
    from Student student
    where (
      :searchTerm is null
      or lower(student.name) like lower(concat('%', :searchTerm, '%'))
      or lower(student.studentId) like lower(concat('%', :searchTerm, '%'))
      or lower(student.username) like lower(concat('%', :searchTerm, '%'))
      or lower(student.course) like lower(concat('%', :searchTerm, '%'))
    )
    and (:studentClass is null or lower(student.studentClass) = lower(:studentClass))
    and (:year is null or lower(student.year) = lower(:year))
    and (:status is null or lower(student.status) = lower(:status))
    order by student.name asc
    """)
  List<Student> searchStudents(
    @Param("searchTerm") String searchTerm,
    @Param("studentClass") String studentClass,
    @Param("year") String year,
    @Param("status") String status
  );
}
