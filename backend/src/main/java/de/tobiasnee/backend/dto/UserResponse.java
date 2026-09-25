package de.tobiasnee.backend.dto;

import de.tobiasnee.backend.entity.UserEntity;

import java.time.Instant;

public record UserResponse(
        Long id,
        String username,
        String email,
        String displayName,
        Instant createdAt
) {

    public static UserResponse from(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getCreatedAt()
        );
    }
}