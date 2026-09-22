package de.tobiasnee.backend.repository;

import de.tobiasnee.backend.entity.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    void existsByUsername_findsSavedUser() {
        userRepository.save(new UserEntity("Max", "max@mustermann.com", "Mustermann"));

        assertThat(userRepository.existsByUsername("Max")).isTrue();
        assertThat(userRepository.existsByUsername("unknown")).isFalse();
    }

    @Test
    void existsByEmail_findsSavedUser() {
        userRepository.save(new UserEntity("Max", "max@mustermann.com", "Mustermann"));

        assertThat(userRepository.existsByEmail("max@mustermann.com")).isTrue();
        assertThat(userRepository.existsByEmail("other@mustermann.com")).isFalse();
    }

    @Test
    void save_setsIdAndCreatedAt() {
        var saved = userRepository.save(new UserEntity("Max", "max@mustermann.com", "Mustermann"));

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getCreatedAt()).isNotNull();
    }

    @Test
    void save_failsOnDuplicateUsername() {
        userRepository.saveAndFlush(new UserEntity("Max", "max@mustermann.com", "Mustermann"));

        assertThatThrownBy(() ->
                userRepository.saveAndFlush(new UserEntity("Max", "other@mustermann.com", "Other")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}