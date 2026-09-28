package de.tobiasnee.backend.repository;

import de.tobiasnee.backend.entity.LikeEntity;
import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class LikeRepositoryTest {

    @Autowired
    private LikeRepository likeRepository;

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void save_failsOnDuplicateLike() {
        var user = userRepository.save(new UserEntity("Max", "max@mustermann.com", "Mustermann"));
        var tweet = tweetRepository.save(new TweetEntity(user, "Ein Tweet"));

        likeRepository.saveAndFlush(new LikeEntity(tweet, user));

        assertThatThrownBy(() -> likeRepository.saveAndFlush(new LikeEntity(tweet, user)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void save_allowsLikesFromDifferentUsers() {
        var max = userRepository.save(new UserEntity("Max", "max@mustermann.com", "Mustermann"));
        var franz = userRepository.save(new UserEntity("Franz", "franz@franz.com", "Franziskus"));
        var tweet = tweetRepository.save(new TweetEntity(max, "Ein Tweet"));

        likeRepository.saveAndFlush(new LikeEntity(tweet, max));
        likeRepository.saveAndFlush(new LikeEntity(tweet, franz));

        assertThat(likeRepository.countByTweetId(tweet.getId())).isEqualTo(2);
    }

    @Test
    void existsByTweetIdAndUserId_findsExistingLike() {
        var user = userRepository.save(new UserEntity("Max", "max@mustermann.com", "Mustermann"));
        var tweet = tweetRepository.save(new TweetEntity(user, "Ein Tweet"));
        likeRepository.saveAndFlush(new LikeEntity(tweet, user));

        assertThat(likeRepository.existsByTweetIdAndUserId(tweet.getId(), user.getId())).isTrue();
        assertThat(likeRepository.existsByTweetIdAndUserId(tweet.getId(), 999L)).isFalse();
    }
}