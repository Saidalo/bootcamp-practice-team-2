package org.demo.banking.application.services;

import org.demo.banking.application.dto.AccountRequestDTO;
import org.demo.banking.application.exception.AccountNotFoundException;
import org.demo.banking.application.exception.IbanAlreadyExistsException;
import org.demo.banking.application.exception.InvalidRequestException;
import org.demo.banking.application.models.Account;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Comparator;
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
        return createAccount(accountRequest.getOwnerName(), accountRequest.getIban(), accountRequest.getInitialBalance());
    }

    // if iban is empty, a new one is generated
    public synchronized Account createAccount(String ownerName, String iban, BigDecimal initialBalance) {
        if (ownerName == null || ownerName.isBlank()) {
            throw new InvalidRequestException("Owner name is required");
        }
        BigDecimal balance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        if (balance.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidRequestException("Initial balance cannot be negative");
        }

        String accountIban = (iban == null || iban.isBlank()) ? generateIban() : normalize(iban);
        if (findByIban(accountIban).isPresent()) {
            throw new IbanAlreadyExistsException(accountIban);
        }

        Account account = new Account();
        account.setId(generatedId.getAndIncrement());
        account.setIban(accountIban);
        account.setOwnerName(ownerName.trim());
        account.setBalance(balance);

        accountStorage.put(account.getId(), account);
        return account;
    }

    public Account getAccountById(Long id) {
        if (id == null) {
            throw new AccountNotFoundException(id);
        }
        return Optional.ofNullable(accountStorage.get(id)).orElseThrow(() -> new AccountNotFoundException(id));
    }

    public Account getAccountByIban(String iban) {
        if (iban == null) {
            throw new AccountNotFoundException(iban);
        }
        return findByIban(normalize(iban)).orElseThrow(() -> new AccountNotFoundException(iban));
    }

    public List<Account> getAllAccounts() {
        return accountStorage.values().stream()
                .sorted(Comparator.comparingLong(Account::getId))
                .toList();
    }

    public void updateAccount(Account account) {
        if (!accountStorage.containsKey(account.getId())) {
            throw new AccountNotFoundException(account.getId());
        }
        accountStorage.put(account.getId(), account);
    }

    private Optional<Account> findByIban(String cleanIban) {
        return accountStorage.values().stream()
                .filter(account -> account.getIban().equals(cleanIban))
                .findFirst();
    }

    // "lt60 1010 ..." and "LT601010..." are the same IBAN
    private static String normalize(String iban) {
        return iban.replace(" ", "").toUpperCase();
    }

    private String generateIban() {
        String iban;
        do {
            iban = "LT" + UUID.randomUUID().toString().substring(0, 16).replace("-", "").toUpperCase();
        } while (findByIban(iban).isPresent());
        return iban;
    }
}
