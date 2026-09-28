package de.tobiasnee.backend.dto;

import de.tobiasnee.backend.entity.UserEntity;

public record TweetAuthor(
        Long id,
        String username,
        String displayName
) {

    public static TweetAuthor from(UserEntity author) {
        return new TweetAuthor(author.getId(), author.getUsername(), author.getDisplayName());
    }
}