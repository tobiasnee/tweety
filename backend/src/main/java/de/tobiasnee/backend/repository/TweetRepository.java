package de.tobiasnee.backend.repository;

import de.tobiasnee.backend.entity.TweetEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TweetRepository extends JpaRepository<TweetEntity, Long> {

    List<TweetEntity> findAllByOrderByCreatedAtDesc();

    List<TweetEntity> findAllByAuthorIdOrderByCreatedAtDesc(Long authorId);
}