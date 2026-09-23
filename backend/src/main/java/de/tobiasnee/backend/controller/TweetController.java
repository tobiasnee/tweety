package de.tobiasnee.backend.controller;

import de.tobiasnee.backend.dto.CreateTweetRequest;
import de.tobiasnee.backend.dto.TweetResponse;
import de.tobiasnee.backend.service.TweetService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

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
    public List<TweetResponse> getAllTweets() { return tweetService.getAllTweets(); }
}