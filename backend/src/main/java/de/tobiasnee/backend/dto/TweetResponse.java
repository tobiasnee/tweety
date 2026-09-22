package de.tobiasnee.backend.dto;

import java.time.Instant;

public record TweetResponse(
        Long id,
        String text,
        UserResponse author,
        Instant createdAt
) {}