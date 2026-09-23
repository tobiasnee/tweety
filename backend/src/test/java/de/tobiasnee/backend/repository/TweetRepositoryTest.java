package de.tobiasnee.backend.repository;

import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class TweetRepositoryTest {

    @Autowired
    private TweetRepository tweetRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void findTimeline_returnsNewestFirst() {
        var author = userRepository.save(new UserEntity("Max", "max@mustermann.com", "Mustermann"));
        tweetRepository.save(new TweetEntity(author, "Mein erster Tweet"));
        tweetRepository.save(new TweetEntity(author, "Mein zweiter Tweet"));

        var result = tweetRepository.findTimeline(PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting("text")
                .containsExactly("Mein zweiter Tweet", "Mein erster Tweet");
    }

    @Test
    void findTimelineByAuthor_returnsOnlyTweetsOfAuthor() {
        var max = userRepository.save(new UserEntity("Max", "max@mustermann.com", "Mustermann"));
        var franz = userRepository.save(new UserEntity("Franz", "franz@franz.com", "Franziskus"));

        tweetRepository.save(new TweetEntity(max, "Max erster Tweet"));
        tweetRepository.save(new TweetEntity(franz, "Franz erster Tweet"));
        tweetRepository.save(new TweetEntity(max, "Max zweiter Tweet"));

        var result = tweetRepository.findTimelineByAuthor(max.getId(), PageRequest.of(0, 10));

        assertThat(result.getContent()).extracting("text")
                .containsExactly("Max zweiter Tweet", "Max erster Tweet");
    }
}