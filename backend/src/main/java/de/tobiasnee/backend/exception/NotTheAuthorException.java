package de.tobiasnee.backend.exception;

public class NotTheAuthorException extends RuntimeException {

    public NotTheAuthorException(String message) { super(message); }
}