package de.tobiasnee.backend.event.listener;

import de.tobiasnee.backend.event.TweetCreatedEvent;
import de.tobiasnee.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SmsListener {

    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onTweetCreated(TweetCreatedEvent event) {
        userRepository.findById(event.authorId()).ifPresent(author ->
                log.info("SMS an {} simuliert: Dein Tweet {} wurde veröffentlicht.",
                        author.getDisplayName(), event.tweetId()));
    }
}