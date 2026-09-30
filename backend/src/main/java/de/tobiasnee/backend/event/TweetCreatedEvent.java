package de.tobiasnee.backend.event;

public record TweetCreatedEvent(Long tweetId, Long authorId) {}