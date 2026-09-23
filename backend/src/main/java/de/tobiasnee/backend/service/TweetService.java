package de.tobiasnee.backend.service;

import de.tobiasnee.backend.dto.CreateTweetRequest;
import de.tobiasnee.backend.dto.TweetResponse;
import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.entity.UserEntity;
import de.tobiasnee.backend.exception.UserNotFoundException;
import de.tobiasnee.backend.repository.TweetRepository;
import de.tobiasnee.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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
    public List<TweetResponse> getAllTweets() {
        return tweetRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(TweetResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<TweetResponse> getTweetsByAuthorId(Long authorId) {
        return tweetRepository.findAllByAuthorIdOrderByCreatedAtDesc(authorId).stream()
                .map(TweetResponse::from)
                .toList();
    }
}