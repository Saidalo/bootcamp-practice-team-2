package org.demo.banking.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<ApiError> handleAccountNotFound(AccountNotFoundException ex) {
        ApiError error = new ApiError(
                HttpStatus.NOT_FOUND.value(),
                "Account not found",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(InsufficientFundsException.class)
    public ResponseEntity<ApiError> handleInsufficientFunds(InsufficientFundsException ex) {
        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "Insufficient funds",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(IbanAlreadyExistsException.class)
    public ResponseEntity<ApiError> handleIbanAlreadyExists(IbanAlreadyExistsException ex) {
        ApiError error = new ApiError(
                HttpStatus.CONFLICT.value(),
                "IBAN already exists",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiError> handleInvalidRequest(InvalidRequestException ex) {
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> handleIllegalArgument(IllegalArgumentException ex) {
        ApiError error = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Invalid request parameter",
                ex.getMessage(),
                Instant.now()
        );
        return ResponseEntity.badRequest().body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        StringBuilder strBuilder = new StringBuilder();

        e.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName;
            try {
                fieldName = ((FieldError) error).getField();
            } catch (ClassCastException ex) {
                fieldName = error.getObjectName();
            }
            String message = error.getDefaultMessage();
            strBuilder.append(String.format("%s: %s\n", fieldName, message));
        });

        String errors = strBuilder.substring(0, strBuilder.length() - 1);
        ApiError apiError = new ApiError(
                HttpStatus.BAD_REQUEST.value(),
                "Validation failed",
                errors,
                Instant.now()
        );
        return ResponseEntity.badRequest().body(apiError);
    }
}