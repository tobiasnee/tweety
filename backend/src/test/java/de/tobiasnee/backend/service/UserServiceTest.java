package de.tobiasnee.backend.service;

import de.tobiasnee.backend.dto.CreateUserRequest;
import de.tobiasnee.backend.entity.UserEntity;
import de.tobiasnee.backend.exception.DuplicateUserException;
import de.tobiasnee.backend.exception.UserNotFoundException;
import de.tobiasnee.backend.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private final CreateUserRequest request =
            new CreateUserRequest("Max", "max@mustermann.com", "Mustermann");

    @Test
    void createUser_savesUser() {
        when(userRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        var response = userService.createUser(request);

        assertThat(response.username()).isEqualTo("Max");
        verify(userRepository).save(any());
    }

    @Test
    void createUser_failsOnDuplicateUsername() {
        when(userRepository.existsByUsername("Max")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateUserException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_failsOnDuplicateEmail() {
        when(userRepository.existsByEmail("max@mustermann.com")).thenReturn(true);

        assertThatThrownBy(() -> userService.createUser(request))
                .isInstanceOf(DuplicateUserException.class);
        verify(userRepository, never()).save(any());
    }

    @Test
    void getUserById_returnsUser() {
        var entity = new UserEntity("Max", "max@mustermann.com", "Mustermann");
        when(userRepository.findById(1L)).thenReturn(Optional.of(entity));

        assertThat(userService.getUserById(1L).username()).isEqualTo("Max");
    }

    @Test
    void getAllUsers_returnsAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(
                new UserEntity("Max", "max@mustermann.com", "Mustermann"),
                new UserEntity("Erika", "erika@mustermann.com", "Musterfrau")
        ));

        var result = userService.getAllUsers();

        assertThat(result).hasSize(2);
        assertThat(result.get(0).username()).isEqualTo("Max");
        assertThat(result.get(1).username()).isEqualTo("Erika");
    }

    @Test
    void getUserById_failsWhenMissing() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(UserNotFoundException.class);
    }
}