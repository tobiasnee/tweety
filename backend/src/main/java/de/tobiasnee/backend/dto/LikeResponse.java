package de.tobiasnee.backend.dto;

public record LikeResponse(
        Long tweetId,
        long likeCount,
        boolean likedByMe
) {}