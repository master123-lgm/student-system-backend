package com.example.studentsystembackend.auth;

public record LoginResponse(String token, String username, Role role) {
}
