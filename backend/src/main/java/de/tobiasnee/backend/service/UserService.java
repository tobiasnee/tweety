package de.tobiasnee.backend.service;

import de.tobiasnee.backend.dto.CreateUserRequest;
import de.tobiasnee.backend.dto.UserResponse;
import de.tobiasnee.backend.entity.UserEntity;
import de.tobiasnee.backend.exception.DuplicateUserException;
import de.tobiasnee.backend.exception.UserNotFoundException;
import de.tobiasnee.backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;


    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException(
                    "Benutzername '" + request.username() + "' ist bereits vergeben.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException(
                    "E-Mail '" + request.email() + "' ist bereits vergeben.");
        }

        UserEntity saved = userRepository.save(
                new UserEntity(request.username(), request.email(), request.displayName()));

        return UserResponse.from(saved);
    }

    public UserResponse getUserById(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(
                        "Benutzer mit ID " + id + " wurde nicht gefunden."));

        return UserResponse.from(entity);
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserResponse::from)
                .toList();
    }
}