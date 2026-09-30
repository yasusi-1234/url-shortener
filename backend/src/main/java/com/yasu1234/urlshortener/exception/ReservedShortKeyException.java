package com.yasu1234.urlshortener.exception;

public class ReservedShortKeyException extends RuntimeException {

    public ReservedShortKeyException(String key) {
        super("reserved short key: " + key);
    }
}
