package com.yasu1234.urlshortener.exception;

import com.yasu1234.urlshortener.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("invalid url format"));
    }

    @ExceptionHandler(UrlNotFoundException.class)
    public ResponseEntity<Void> handleNotFound(UrlNotFoundException ex) {
        return ResponseEntity.notFound().build();
    }

    @ExceptionHandler(KeyGenerationException.class)
    public ResponseEntity<ErrorResponse> handleKeyGeneration(KeyGenerationException ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("failed to generate short key"));
    }
}
