package de.tobiasnee.backend.dto;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        Instant createdAt
) {}