package org.demo.banking.application.models;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * One line in the account history. Immutable: a transaction never changes after it is created.
 */
@Getter
@AllArgsConstructor
public class Transaction {
    private final long id;
    private final long accountId;
    private final TransactionType type;
    private final BigDecimal amount;
    private final Instant createdAt;
    private final String note;
}