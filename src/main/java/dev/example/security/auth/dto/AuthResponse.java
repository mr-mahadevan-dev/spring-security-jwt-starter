package dev.example.security.auth.dto;

public record AuthResponse(String tokenType, String accessToken, long expiresIn, String refreshToken, String email, String role) {}
