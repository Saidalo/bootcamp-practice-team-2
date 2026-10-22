package org.demo.banking.application.services;

import org.demo.banking.application.dto.AccountRequestDTO;
import org.demo.banking.application.dto.AccountResponseDTO;
import org.demo.banking.application.dto.TransactionResponseDTO;
import org.springframework.stereotype.Service;


import org.demo.banking.application.models.Account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class AccountService {
    private final Map<Long, Account> accountStorage = new ConcurrentHashMap<>();

    private final AtomicLong generatedId = new AtomicLong(1);
    public Account createAccount(AccountRequestDTO accountRequest) {
        Account account = new Account();
        account.setId(generatedId.getAndIncrement());
        account.setIban("LT" + UUID.randomUUID().toString().substring(0, 16).replace("-", ""));
        account.setOwnerName(ownerName);
        account.setBalance(curBalance);

        accountStorage.put(account.getId(), account);
        return account;
    }


    public AccountResponseDTO getAccountById(Long id) {
        return Optional.ofNullable(accountStorage.get(id)).orElseThrow(() -> new IllegalArgumentException("Account with id " + id + " not found"));
    }
    public void updateAccount(Account account) {
        if (!accountStorage.containsKey(account.getId())) {
            throw new IllegalArgumentException("Cannot update non-existing account");
        }
        accountStorage.put(account.getId(), account);
    }

    public List<TransactionResponseDTO> getTransactions(Long id) {
    }
}
