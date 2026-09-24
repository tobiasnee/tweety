package de.tobiasnee.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTweetRequest(

        @NotNull(message = "Autor muss angegeben werden.")
        Long authorId,

        @NotBlank(message = "Text darf nicht leer sein.")
        String text

) {}