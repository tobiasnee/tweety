package de.tobiasnee.backend.dto;

import de.tobiasnee.backend.entity.TweetEntity;

import java.time.Instant;

public record TweetResponse(
        Long id,
        String text,
        UserResponse author,
        Instant createdAt,
        int likeCount
) {

    public static TweetResponse from(TweetEntity tweet) {
        return new TweetResponse(
                tweet.getId(),
                tweet.getText(),
                UserResponse.from(tweet.getAuthor()),
                tweet.getCreatedAt(),
                0
        );
    }
}