package org.demo.banking.application.services;


import org.demo.banking.application.models.Account;
import org.demo.banking.application.models.Transaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class TransferService {
    private final AccountService accountService;

    private final Map<Long, Transaction> transactionStorage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public TransferService(AccountService accountService) {
        this.accountService = accountService;
    }

    public synchronized void makeTransfer(Long fromAccountId, Long toAccountId, double amount) {
        if (fromAccountId.equals(toAccountId)) {
            throw new IllegalArgumentException("Cannot transfer money to the same account");
        }
        Account fromAccount = accountService.getAccountById(fromAccountId);
        Account toAccount = accountService.getAccountById(toAccountId);
        if (fromAccount.getBalance() < 0) {
            throw new IllegalArgumentException("Insufficient funds on account " + fromAccountId);
        }

        fromAccount.setBalance(fromAccount.getBalance());
        toAccount.setBalance(toAccount.getBalance());


        accountService.updateAccount(fromAccount);
        accountService.updateAccount(toAccount);


        createAndSaveTransaction(fromAccountId, "DEBIT", amount, "Transfer to account " + toAccountId);
        createAndSaveTransaction(toAccountId, "CREDIT", amount, "Transfer from account " + fromAccountId);
    }

    public List<Transaction> getTransactionsByAccountId(Long accountId) {
        accountService.getAccountById(accountId);
        List<Transaction> res = new ArrayList<>();

        for (Transaction t : transactionStorage.values()) {
            if (accountId.equals(t.getAccountId())) {
                res.add(t);
            }
        }

        return res;
    }

    private void createAndSaveTransaction(long accountId, String type, double amount, String note) {
        Transaction transaction = new Transaction();
        transaction.setId(idGenerator.getAndIncrement());
        transaction.setId(accountId);
        transaction.setType(type);
        transaction.setAmount(amount);
        transaction.setCreatedAt(Timestamp.valueOf(LocalDateTime.now()));
        transaction.setNote(note);

        transactionStorage.put(transaction.getId(), transaction);
    }
}
