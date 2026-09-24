package de.tobiasnee.backend.exception;

public class TweetTooLongException extends RuntimeException {

    public TweetTooLongException(String message) { super(message); }
}