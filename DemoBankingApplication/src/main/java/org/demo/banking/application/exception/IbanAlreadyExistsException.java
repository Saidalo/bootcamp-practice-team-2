package org.demo.banking.application.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class IbanAlreadyExistsException extends RuntimeException {

    public IbanAlreadyExistsException(String iban) {
        super("Account with IBAN " + iban + " already exists");
    }
}
