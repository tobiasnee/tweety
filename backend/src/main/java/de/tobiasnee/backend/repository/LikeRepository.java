package de.tobiasnee.backend.repository;

import de.tobiasnee.backend.entity.LikeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LikeRepository extends JpaRepository<LikeEntity, Long> {

    boolean existsByTweetIdAndUserId(Long tweetId, Long userId);

    Optional<LikeEntity> findByTweetIdAndUserId(Long tweetId, Long userId);

    long countByTweetId(Long tweetId);
}