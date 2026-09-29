package de.tobiasnee.backend.event.listener;

import de.tobiasnee.backend.entity.AuditEntity;
import de.tobiasnee.backend.event.TweetCreatedEvent;
import de.tobiasnee.backend.repository.AuditRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuditListener {

    private static final String ACTION = "TWEET_CREATED";

    private final AuditRepository auditRepository;

    @EventListener
    public void onTweetCreated(TweetCreatedEvent event) {
        auditRepository.save(new AuditEntity(ACTION, event.tweetId(), event.authorId()));
        log.info("Audit-Eintrag für Tweet {} gespeichert.", event.tweetId());
    }
}