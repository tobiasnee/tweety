package de.tobiasnee.backend.service.postprocessing;

import de.tobiasnee.backend.entity.TweetEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class SmsTweetCreatedService implements TweetCreatedService {

    @Override
    public void handle(TweetEntity tweet) {
        log.info("SMS an {} simuliert: Dein Tweet {} wurde veröffentlicht.",
                tweet.getAuthor().getDisplayName(), tweet.getId());
    }
}