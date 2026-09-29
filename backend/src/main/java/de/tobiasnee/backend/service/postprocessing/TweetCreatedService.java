package de.tobiasnee.backend.service.postprocessing;

import de.tobiasnee.backend.entity.TweetEntity;

public interface TweetCreatedService {

    void handle(TweetEntity tweet);
}