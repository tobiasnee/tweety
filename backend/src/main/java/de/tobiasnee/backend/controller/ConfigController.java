package de.tobiasnee.backend.controller;

import de.tobiasnee.backend.dto.AppConfigResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/config")
public class ConfigController {

    private final int maxTweetLength;

    public ConfigController(@Value("${app.max-tweet-length}") int maxTweetLength) {
        this.maxTweetLength = maxTweetLength;
    }

    @GetMapping
    public AppConfigResponse getConfig() {
        return new AppConfigResponse(maxTweetLength);
    }
}