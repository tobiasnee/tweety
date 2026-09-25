package de.tobiasnee.backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateTweetRequest(

        @NotNull(message = "Bearbeiter muss angegeben werden.")
        Long editorId,

        @NotBlank(message = "Text darf nicht leer sein.")
        String text

) {}