package de.tobiasnee.backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateUserRequest(

        @NotBlank(message = "Benutzername darf nicht leer sein.")
        String username,

        @NotBlank(message = "E-Mail darf nicht leer sein.")
        @Email(message = "E-Mail ist keine gültige Adresse.")
        String email,

        @NotBlank(message = "Anzeigename darf nicht leer sein.")
        String displayName

) {}