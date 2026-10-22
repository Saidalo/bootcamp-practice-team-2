package org.demo.banking.application.services;

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
    public Account createAccount(String ownerName, double curBalance) {
        Account account = new Account();
        account.setId(generatedId.getAndIncrement());
        account.setIban("LT" + UUID.randomUUID().toString().substring(0, 16).replace("-", ""));
        account.setOwnerName(ownerName);
        account.setBalance(curBalance != null ? curBalance : 0.0);

        accountStorage.put(account.getId(), account);
        return account;
    }


    public Account getAccountById(Long id) {
        return Optional.ofNullable(accountStorage.get(id)).orElseThrow(() -> new IllegalArgumentException("Account with id " + id + " not found"));
    }
    public void updateAccount(Account account) {
        if (!accountStorage.containsKey(account.getId())) {
            throw new IllegalArgumentException("Cannot update non-existing account");
        }
        accountStorage.put(account.getId(), account);
    }
}
