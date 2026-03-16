package com.group.hackathon_G5Dev.api.dto.response;

public record AuthResponse(
        String token,
        Long userId,
        String email,
        String firstName,
        String lastName,
        String role
) {}
