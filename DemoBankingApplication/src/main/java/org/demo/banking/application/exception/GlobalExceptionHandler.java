package org.demo.banking.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
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
        return new ResponseEntity<>(Map.of("errors", errors), HttpStatus.BAD_REQUEST);
    }

    // GET /account/12 when account 12 does not exist
    @ExceptionHandler(AccountNotFoundException.class)
    public ResponseEntity<Object> handleAccountNotFound(AccountNotFoundException e) {
        return error(HttpStatus.NOT_FOUND, e.getMessage());
    }

    // wrong amount, same account, empty owner name...
    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<Object> handleInvalidRequest(InvalidRequestException e) {
        return error(HttpStatus.BAD_REQUEST, e.getMessage());
    }

    // not enough money, IBAN already used
    @ExceptionHandler({InsufficientFundsException.class, IbanAlreadyExistsException.class})
    public ResponseEntity<Object> handleConflict(RuntimeException e) {
        return error(HttpStatus.CONFLICT, e.getMessage());
    }

    // body is empty or not valid JSON
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Object> handleBadJson(HttpMessageNotReadableException e) {
        return error(HttpStatus.BAD_REQUEST, "Request body is missing or is not valid JSON");
    }

    // GET /account/abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Object> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return error(HttpStatus.BAD_REQUEST, "Wrong value '" + e.getValue() + "' for " + e.getName());
    }

    // URL that does not exist, e.g. /acount
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<Object> handleUnknownUrl(NoResourceFoundException e) {
        return error(HttpStatus.NOT_FOUND, "Endpoint not found: /" + e.getResourcePath());
    }

    private ResponseEntity<Object> error(HttpStatus status, String message) {
        return new ResponseEntity<>(Map.of("errors", message), status);
    }
}
