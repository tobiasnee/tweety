package de.tobiasnee.backend.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Objects;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String displayName;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    public UserEntity(String username, String email, String displayName) {
        this.username = Objects.requireNonNull(username, "username must not be null");
        this.email = Objects.requireNonNull(email, "email must not be null");
        this.displayName = Objects.requireNonNull(displayName, "displayName must not be null");
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }
}