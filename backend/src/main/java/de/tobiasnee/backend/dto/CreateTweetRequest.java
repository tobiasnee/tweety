package de.tobiasnee.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateTweetRequest(

        @NotNull(message = "Autor muss angegeben werden.")
        Long authorId,

        @NotBlank(message = "Text darf nicht leer sein.")
        @Size(max = 280, message = "Text darf höchstens 280 Zeichen lang sein.")
        String text

) {}