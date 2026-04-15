package com.example.forms.auth.dto;

public record AuthResponse(
    String accessToken,
    Long userId,
    String email,
    String name
) {
}
