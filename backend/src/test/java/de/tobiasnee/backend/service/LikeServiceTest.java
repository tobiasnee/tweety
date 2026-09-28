package de.tobiasnee.backend.service;

import de.tobiasnee.backend.entity.LikeEntity;
import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.entity.UserEntity;
import de.tobiasnee.backend.exception.TweetNotFoundException;
import de.tobiasnee.backend.repository.LikeRepository;
import de.tobiasnee.backend.repository.TweetRepository;
import de.tobiasnee.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LikeServiceTest {

    @Mock
    private LikeRepository likeRepository;

    @Mock
    private TweetRepository tweetRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private LikeService likeService;

    private final UserEntity user = userWithId();
    private final TweetEntity tweet = tweetWithId();

    @Test
    void like_savesLike() {
        when(likeRepository.existsByTweetIdAndUserId(1L, 1L)).thenReturn(false);
        when(tweetRepository.findById(1L)).thenReturn(Optional.of(tweet));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(likeRepository.countByTweetId(1L)).thenReturn(1L);

        var response = likeService.like(1L, 1L);

        assertThat(response.likeCount()).isEqualTo(1L);
        assertThat(response.likedByMe()).isTrue();
        verify(likeRepository).save(any(LikeEntity.class));
    }

    @Test
    void like_doesNothingWhenAlreadyLiked() {
        when(likeRepository.existsByTweetIdAndUserId(1L, 1L)).thenReturn(true);
        when(likeRepository.countByTweetId(1L)).thenReturn(1L);

        var response = likeService.like(1L, 1L);

        assertThat(response.likeCount()).isEqualTo(1L);
        verify(likeRepository, never()).save(any());
    }

    @Test
    void like_failsWhenTweetMissing() {
        when(likeRepository.existsByTweetIdAndUserId(99L, 1L)).thenReturn(false);
        when(tweetRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> likeService.like(99L, 1L))
                .isInstanceOf(TweetNotFoundException.class);
        verify(likeRepository, never()).save(any());
    }

    @Test
    void unlike_removesLike() {
        var like = new LikeEntity(tweet, user);
        when(likeRepository.findByTweetIdAndUserId(1L, 1L)).thenReturn(Optional.of(like));
        when(likeRepository.countByTweetId(1L)).thenReturn(0L);

        var response = likeService.unlike(1L, 1L);

        assertThat(response.likeCount()).isZero();
        assertThat(response.likedByMe()).isFalse();
        verify(likeRepository).delete(like);
    }

    @Test
    void unlike_doesNothingWhenNotLiked() {
        when(likeRepository.findByTweetIdAndUserId(1L, 1L)).thenReturn(Optional.empty());
        when(likeRepository.countByTweetId(1L)).thenReturn(0L);

        likeService.unlike(1L, 1L);

        verify(likeRepository, never()).delete(any());
    }

    private static UserEntity userWithId() {
        var user = new UserEntity("Max", "max@mustermann.com", "Mustermann");
        ReflectionTestUtils.setField(user, "id", 1L);
        return user;
    }

    private static TweetEntity tweetWithId() {
        var tweet = new TweetEntity(userWithId(), "Ein Tweet");
        ReflectionTestUtils.setField(tweet, "id", 1L);
        return tweet;
    }
}