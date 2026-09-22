package de.tobiasnee.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "tweets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TweetEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 280)
    private String text;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_id")
    private UserEntity author;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public TweetEntity(@NonNull UserEntity author, String text) {
        this.author = author;
        this.text = text;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}