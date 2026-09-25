package de.tobiasnee.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;

@Entity
@Table(name = "likes", uniqueConstraints = @UniqueConstraint(
        name = "uk_likes_tweet_user", columnNames = {"tweet_id", "user_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LikeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tweet_id")
    private TweetEntity tweet;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public LikeEntity(@NonNull TweetEntity tweet, @NonNull UserEntity user) {
        this.tweet = tweet;
        this.user = user;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}