package com.yasu1234.urlshortener.exception;

public class UrlNotFoundException extends RuntimeException {

    public UrlNotFoundException(String shortKey) {
        super("short key not found: " + shortKey);
    }
}
