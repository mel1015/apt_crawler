package com.example.aptcrawler.client;

public class AptApiException extends RuntimeException {
    public AptApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
