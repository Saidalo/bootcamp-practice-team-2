package org.demo.banking.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(Long id) {
        super("Account with id " + id + " not found");
    }

    public AccountNotFoundException(String iban) {
        super("Account with IBAN " + iban + " not found");
    }
}
