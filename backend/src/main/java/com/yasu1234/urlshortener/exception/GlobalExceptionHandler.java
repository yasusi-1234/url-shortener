package com.yasu1234.urlshortener.exception;

import com.yasu1234.urlshortener.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError != null ? fieldError.getDefaultMessage() : "invalid request";
        return ResponseEntity.badRequest().body(new ErrorResponse(message));
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

    @ExceptionHandler(ReservedShortKeyException.class)
    public ResponseEntity<ErrorResponse> handleReservedShortKey(ReservedShortKeyException ex) {
        return ResponseEntity.badRequest().body(new ErrorResponse("このキーは予約されているため使用できません"));
    }

    @ExceptionHandler(CustomKeyAlreadyInUseException.class)
    public ResponseEntity<ErrorResponse> handleCustomKeyConflict(CustomKeyAlreadyInUseException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(new ErrorResponse("そのキーは既に使われています"));
    }
}
