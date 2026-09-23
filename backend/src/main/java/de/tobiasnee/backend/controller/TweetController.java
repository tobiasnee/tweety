package de.tobiasnee.backend.controller;

import de.tobiasnee.backend.dto.CreateTweetRequest;
import de.tobiasnee.backend.dto.TweetResponse;
import de.tobiasnee.backend.service.TweetService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/api/tweets")
public class TweetController {

    private final TweetService tweetService;

    public TweetController(TweetService tweetService) {
        this.tweetService = tweetService;
    }

    @PostMapping
    public ResponseEntity<TweetResponse> createTweet(@Valid @RequestBody CreateTweetRequest request) {
        TweetResponse response = tweetService.createTweet(request);

        URI location = URI.create("/api/tweets/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public Page<TweetResponse> getAllTweets(@PageableDefault(size = 20) Pageable pageable) {
        return tweetService.getAllTweets(pageable);
    }
}