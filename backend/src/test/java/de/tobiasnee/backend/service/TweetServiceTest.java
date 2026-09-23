package de.tobiasnee.backend.service;

import de.tobiasnee.backend.dto.CreateTweetRequest;
import de.tobiasnee.backend.repository.projection.TweetListItem;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import java.time.Instant;import de.tobiasnee.backend.entity.UserEntity;
import de.tobiasnee.backend.exception.UserNotFoundException;
import de.tobiasnee.backend.repository.TweetRepository;
import de.tobiasnee.backend.repository.UserRepository;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TweetServiceTest {

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private TweetService tweetService;

    private final UserEntity author = new UserEntity("Max", "max@mustermann.com", "Mustermann");


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
    void getAllTweets_returnsMappedTweets() {
        when(tweetRepository.findTimeline(any())).thenReturn(new PageImpl<>(List.of(
                new TweetListItem(2L, "Neuester Tweet", Instant.now(), 1L, "Max", "Mustermann"),
                new TweetListItem(1L, "Älterer Tweet", Instant.now(), 1L, "Max", "Mustermann")
        )));

        var result = tweetService.getAllTweets(PageRequest.of(0, 20));

        assertThat(result.getContent()).extracting("text")
                .containsExactly("Neuester Tweet", "Älterer Tweet");
    }}