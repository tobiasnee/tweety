package de.tobiasnee.backend.dto;

import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.repository.projection.TweetListItem;

import java.time.Instant;

public record TweetResponse(
        Long id,
        String text,
        TweetAuthor author,
        Instant createdAt,
        int likeCount
) {

    public static TweetResponse from(TweetEntity tweet) {
        return new TweetResponse(
                tweet.getId(),
                tweet.getText(),
                TweetAuthor.from(tweet.getAuthor()),
                tweet.getCreatedAt(),
                0
        );
    }

    public static TweetResponse from(TweetListItem item) {
        return new TweetResponse(
                item.id(),
                item.text(),
                new TweetAuthor(item.authorId(), item.authorUsername(), item.authorDisplayName()),
                item.createdAt(),
                0
        );
    }
}