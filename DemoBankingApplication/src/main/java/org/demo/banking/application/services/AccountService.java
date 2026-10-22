package org.demo.banking.application.services;

import org.demo.banking.application.exceptions.AccountNotFoundException;
import org.springframework.stereotype.Service;


import org.demo.banking.application.models.Account;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AccountService {
    private final Map<Long, Account> accountStorage = new ConcurrentHashMap<>();

    private final AtomicLong generatedId = new AtomicLong(1);
    public Account createAccount(String ownerName, BigDecimal curBalance) {
        Account account = new Account();
        account.setId(generatedId.getAndIncrement());
        account.setIban("LT" + UUID.randomUUID().toString().substring(0, 16).replace("-", ""));
        account.setOwnerName(ownerName);
        account.setBalance(curBalance != null ? curBalance : BigDecimal.ZERO);

        accountStorage.put(account.getId(), account);
        return account;
    }


    public Account getAccountById(Long id) {
        return Optional.ofNullable(accountStorage.get(id)).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account getAccountByIban(String iban) {
        return accountStorage.values().stream()
                .filter(account -> account.getIban().equalsIgnoreCase(iban.trim()))
                .findFirst()
                .orElseThrow(() -> new AccountNotFoundException(iban));
    }

    public void updateAccount(Account account) {
        if (!accountStorage.containsKey(account.getId())) {
            throw new IllegalArgumentException("Cannot update non-existing account");
        }
        accountStorage.put(account.getId(), account);
    }
}