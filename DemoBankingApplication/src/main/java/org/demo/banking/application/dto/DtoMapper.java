package org.demo.banking.application.dto;

import org.demo.banking.application.models.Account;
import org.demo.banking.application.models.Transaction;

/**
 * Converts models to the DTOs that are returned as JSON.
 */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static AccountResponseDTO toAccountResponse(Account account) {
        return new AccountResponseDTO(account.getId(), account.getIban(), account.getOwnerName(), account.getBalance());
    }

    public static TransactionResponseDTO toTransactionResponse(Transaction transaction) {
        return new TransactionResponseDTO(transaction.getId(), transaction.getAccountId(),
                transaction.getType().name(), transaction.getAmount(), transaction.getCreatedAt(), transaction.getNote());
    }
}
