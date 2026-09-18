package de.tobiasnee.backend.dto;

public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName
) {}