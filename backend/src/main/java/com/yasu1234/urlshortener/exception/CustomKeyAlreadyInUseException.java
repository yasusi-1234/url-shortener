package com.yasu1234.urlshortener.exception;

public class CustomKeyAlreadyInUseException extends RuntimeException {

    public CustomKeyAlreadyInUseException(String key) {
        super("custom key already in use: " + key);
    }
}
