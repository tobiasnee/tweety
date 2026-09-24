package de.tobiasnee.backend.service;

import de.tobiasnee.backend.dto.CreateTweetRequest;
import de.tobiasnee.backend.dto.UpdateTweetRequest;
import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.entity.UserEntity;
import de.tobiasnee.backend.exception.NotTheAuthorException;
import de.tobiasnee.backend.exception.TweetNotFoundException;
import de.tobiasnee.backend.exception.TweetTooLongException;
import de.tobiasnee.backend.exception.UserNotFoundException;
import de.tobiasnee.backend.repository.TweetRepository;
import de.tobiasnee.backend.repository.UserRepository;
import de.tobiasnee.backend.repository.projection.TweetListItem;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TweetServiceTest {

    private static final int MAX_TWEET_LENGTH = 20;

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    private TweetService tweetService;

    private final UserEntity author = authorWithId();

    @BeforeEach
    void setUp() {
        tweetService = new TweetService(tweetRepository, userRepository, MAX_TWEET_LENGTH);
    }


    @Test
    void createTweetSavesTweet() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(tweetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = tweetService.createTweet(new CreateTweetRequest(1L, "Mein erster Tweet"));

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(response.text()).isEqualTo("Mein erster Tweet");
            softly.assertThat(response.author().username()).isEqualTo("Max");
            softly.assertThat(response.author().displayName()).isEqualTo("Mustermann");
        });
        verify(tweetRepository).save(any());
    }

    @Test
    void createTweet_failsWhenAuthorMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tweetService.createTweet(new CreateTweetRequest(99L, "Hallo")))
                .isInstanceOf(UserNotFoundException.class);
        verify(tweetRepository, never()).save(any());
    }

    @Test
    void createTweet_failsWhenTextTooLong() {
        var tooLong = "x".repeat(MAX_TWEET_LENGTH + 1);

        assertThatThrownBy(() -> tweetService.createTweet(new CreateTweetRequest(1L, tooLong)))
                .isInstanceOf(TweetTooLongException.class);
        verify(tweetRepository, never()).save(any());
    }

    @Test
    void createTweet_acceptsTextAtMaxLength() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(author));
        when(tweetRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var exactly = "x".repeat(MAX_TWEET_LENGTH);

        assertThat(tweetService.createTweet(new CreateTweetRequest(1L, exactly)).text())
                .hasSize(MAX_TWEET_LENGTH);
    }


    @Test
    void getAllTweets_returnsMappedTweets() {
        when(tweetRepository.findTimeline(any())).thenReturn(new PageImpl<>(List.of(
                new TweetListItem(2L, "Neuester Tweet", Instant.now(), 1L, "Max", "Mustermann"),
                new TweetListItem(1L, "Älterer Tweet", Instant.now(), 1L, "Max", "Mustermann")
        )));

        var result = tweetService.getAllTweets(PageRequest.of(0, 20));

        assertThat(result.getContent()).extracting("text")
                .containsExactly("Neuester Tweet", "Älterer Tweet");
    }

    @Test
    void updateTweet_changesText() {
        var tweet = tweetWithId(1L, author, "Alter Text");
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));

        var response = tweetService.updateTweet(1L, new UpdateTweetRequest(1L, "Neuer Text"));

        assertThat(response.text()).isEqualTo("Neuer Text");
    }

    @Test
    void updateTweet_failsWhenTweetMissing() {
        when(tweetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tweetService.updateTweet(99L, new UpdateTweetRequest(1L, "Neu")))
                .isInstanceOf(TweetNotFoundException.class);
    }

    @Test
    void updateTweet_failsWhenNotTheAuthor() {
        var tweet = tweetWithId(1L, author, "Alter Text");
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));

        assertThatThrownBy(() -> tweetService.updateTweet(1L, new UpdateTweetRequest(99L, "Neu")))
                .isInstanceOf(NotTheAuthorException.class);
        assertThat(tweet.getText()).isEqualTo("Alter Text");
    }

    @Test
    void updateTweet_failsWhenTextTooLong() {
        var tooLong = "x".repeat(MAX_TWEET_LENGTH + 1);

        assertThatThrownBy(() -> tweetService.updateTweet(1L, new UpdateTweetRequest(1L, tooLong)))
                .isInstanceOf(TweetTooLongException.class);
    }

    @Test
    void deleteTweet_removesTweet() {
        var tweet = tweetWithId(1L, author, "Text");
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));

        tweetService.deleteTweet(1L, 1L);

        verify(tweetRepository).delete(tweet);
    }

    @Test
    void deleteTweet_failsWhenNotTheAuthor() {
        var tweet = tweetWithId(1L, author, "Text");
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));

        assertThatThrownBy(() -> tweetService.deleteTweet(1L, 99L))
                .isInstanceOf(NotTheAuthorException.class);
        verify(tweetRepository, never()).delete(any());
    }

    private static UserEntity authorWithId() {
        var user = new UserEntity("Max", "max@mustermann.com", "Mustermann");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private static TweetEntity tweetWithId(Long id, UserEntity author, String text) {
        var tweet = new TweetEntity(author, text);
        ReflectionTestUtils.setField(tweet, "id", id);
        return tweet;
    }
}