package com.example.studentsystembackend.auth;

public record AuthUser(String username, Role role) {

  public static final String REQUEST_ATTRIBUTE = "authenticatedUser";
}
