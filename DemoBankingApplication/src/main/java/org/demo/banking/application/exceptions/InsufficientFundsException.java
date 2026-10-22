package org.demo.banking.application.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.math.BigDecimal;

@ResponseStatus(HttpStatus.CONFLICT)
public class InsufficientFundsException extends RuntimeException {

    public InsufficientFundsException(long accountId, BigDecimal balance, BigDecimal amount) {
        super("Not enough money on account " + accountId + ": balance " + balance + ", needed " + amount);
    }
}