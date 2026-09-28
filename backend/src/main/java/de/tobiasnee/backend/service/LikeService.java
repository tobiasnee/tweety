package de.tobiasnee.backend.service;

import de.tobiasnee.backend.dto.LikeResponse;
import de.tobiasnee.backend.entity.LikeEntity;
import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.entity.UserEntity;
import de.tobiasnee.backend.exception.TweetNotFoundException;
import de.tobiasnee.backend.exception.UserNotFoundException;
import de.tobiasnee.backend.repository.LikeRepository;
import de.tobiasnee.backend.repository.TweetRepository;
import de.tobiasnee.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeService {

    private final LikeRepository likeRepository;
    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeResponse like(Long tweetId, Long userId) {
        if (!likeRepository.existsByTweetIdAndUserId(tweetId, userId)) {
            TweetEntity tweet = tweetRepository.findById(tweetId)
                    .orElseThrow(() -> new TweetNotFoundException(
                            "Tweet mit ID " + tweetId + " wurde nicht gefunden."));

            UserEntity user = userRepository.findById(userId)
                    .orElseThrow(() -> new UserNotFoundException(
                            "Benutzer mit ID " + userId + " wurde nicht gefunden."));

            likeRepository.save(new LikeEntity(tweet, user));
        }

        return new LikeResponse(tweetId, likeRepository.countByTweetId(tweetId), true);
    }

    @Transactional
    public LikeResponse unlike(Long tweetId, Long userId) {
        likeRepository.findByTweetIdAndUserId(tweetId, userId)
                .ifPresent(likeRepository::delete);

        return new LikeResponse(tweetId, likeRepository.countByTweetId(tweetId), false);
    }
}