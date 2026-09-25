package de.tobiasnee.backend.repository.projection;

import java.time.Instant;

public record TweetListItem(
        Long id,
        String text,
        Instant createdAt,
        Long authorId,
        String authorUsername,
        String authorDisplayName,
        long likeCount,
        boolean likedByMe
) {}