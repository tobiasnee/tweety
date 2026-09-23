package de.tobiasnee.backend.service;

import de.tobiasnee.backend.dto.CreateTweetRequest;
import de.tobiasnee.backend.dto.TweetResponse;
import de.tobiasnee.backend.dto.UpdateTweetRequest;
import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.entity.UserEntity;
import de.tobiasnee.backend.exception.NotTheAuthorException;
import de.tobiasnee.backend.exception.TweetNotFoundException;
import de.tobiasnee.backend.exception.UserNotFoundException;
import de.tobiasnee.backend.repository.TweetRepository;
import de.tobiasnee.backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TweetService {

    private final TweetRepository tweetRepository;
    private final UserRepository userRepository;

    public TweetService(TweetRepository tweetRepository, UserRepository userRepository) {
        this.tweetRepository = tweetRepository;
        this.userRepository = userRepository;
    }

    @Transactional
    public TweetResponse createTweet(CreateTweetRequest request) {
        UserEntity author = userRepository.findById(request.authorId())
                .orElseThrow(() -> new UserNotFoundException(
                        "Benutzer mit ID " + request.authorId() + " wurde nicht gefunden."));

        TweetEntity saved = tweetRepository.save(new TweetEntity(author, request.text()));
        return TweetResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public Page<TweetResponse> getAllTweets(Pageable pageable) {
        return tweetRepository.findTimeline(pageable).map(TweetResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<TweetResponse> getTweetsByAuthorId(Long authorId, Pageable pageable) {
        return tweetRepository.findTimelineByAuthor(authorId, pageable).map(TweetResponse::from);
    }
    @Transactional
    public TweetResponse updateTweet(Long tweetId, UpdateTweetRequest request) {
        TweetEntity tweet = loadOwnTweet(tweetId, request.editorId());
        tweet.changeText(request.text());
        return TweetResponse.from(tweet);
    }

    @Transactional
    public void deleteTweet(Long tweetId, Long editorId) {
        TweetEntity tweet = loadOwnTweet(tweetId, editorId);
        tweetRepository.delete(tweet);
    }

    private TweetEntity loadOwnTweet(Long tweetId, Long editorId) {
        TweetEntity tweet = tweetRepository.findById(tweetId)
                .orElseThrow(() -> new TweetNotFoundException(
                        "Tweet mit ID " + tweetId + " wurde nicht gefunden."));

        if (!tweet.getAuthor().getId().equals(editorId)) {
            throw new NotTheAuthorException(
                    "Tweet mit ID " + tweetId + " gehört nicht zu Benutzer " + editorId + ".");
        }

        return tweet;
    }
}