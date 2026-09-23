package de.tobiasnee.backend.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class NotTheAuthorException extends RuntimeException {

    public NotTheAuthorException(String message) {
        super(message);
    }
}