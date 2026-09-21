package de.tobiasnee.backend.service;

import de.tobiasnee.backend.dto.CreateUserRequest;
import de.tobiasnee.backend.dto.UserResponse;
import de.tobiasnee.backend.entity.UserEntity;
import de.tobiasnee.backend.exception.DuplicateUserException;
import de.tobiasnee.backend.exception.UserNotFoundException;
import de.tobiasnee.backend.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateUserException(
                    "Benutzername '" + request.username() + "' ist bereits vergeben.");
        }

        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateUserException(
                    "E-Mail '" + request.email() + "' ist bereits vergeben.");
        }

        UserEntity saved = userRepository.save(toEntity(request));
        return toResponse(saved);
    }

    public UserResponse getUserById(Long id) {
        UserEntity entity = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Benutzer mit ID " + id + " wurde nicht gefunden."));

        return toResponse(entity);
    }

    private UserEntity toEntity(CreateUserRequest request) {
        return new UserEntity(request.username(), request.email(), request.displayName());
    }

    private UserResponse toResponse(UserEntity entity) {
        return new UserResponse(
                entity.getId(),
                entity.getUsername(),
                entity.getEmail(),
                entity.getDisplayName(),
                entity.getCreatedAt()
        );
    }
}