package de.tobiasnee.backend.controller;

import de.tobiasnee.backend.dto.LikeResponse;
import de.tobiasnee.backend.service.LikeService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tweets/{tweetId}/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public LikeResponse like(@PathVariable Long tweetId,
                             @RequestParam Long userId) {
        return likeService.like(tweetId, userId);
    }

    @DeleteMapping
    public LikeResponse unlike(@PathVariable Long tweetId,
                               @RequestParam Long userId) {
        return likeService.unlike(tweetId, userId);
    }
}