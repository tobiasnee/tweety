package de.tobiasnee.backend.service.postprocessing;

import de.tobiasnee.backend.entity.AuditEntity;
import de.tobiasnee.backend.entity.TweetEntity;
import de.tobiasnee.backend.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuditTweetCreatedService implements TweetCreatedService {

    private static final String ACTION = "TWEET_CREATED";

    private final AuditRepository auditRepository;

    @Override
    public void handle(TweetEntity tweet) {
        auditRepository.save(new AuditEntity(ACTION, tweet.getId(), tweet.getAuthor().getId()));
        log.info("Audit-Eintrag für Tweet {} gespeichert.", tweet.getId());
    }
}