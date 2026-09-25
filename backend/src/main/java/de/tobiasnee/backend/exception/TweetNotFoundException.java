package de.tobiasnee.backend.exception;

public class TweetNotFoundException extends RuntimeException {

    public TweetNotFoundException(String message) { super(message); }
}