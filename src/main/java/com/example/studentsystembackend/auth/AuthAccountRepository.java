package com.example.studentsystembackend.auth;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthAccountRepository extends JpaRepository<AuthAccount, Long> {

  Optional<AuthAccount> findByUsername(String username);

  boolean existsByUsername(String username);
}
