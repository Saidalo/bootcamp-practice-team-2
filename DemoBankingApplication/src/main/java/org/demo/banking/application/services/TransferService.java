package org.demo.banking.application.services;

import org.demo.banking.application.exception.InsufficientFundsException;
import org.demo.banking.application.exception.InvalidRequestException;
import org.demo.banking.application.models.Account;
import org.demo.banking.application.models.Transaction;
import org.demo.banking.application.models.TransactionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Money transfers between accounts and the transaction history.
 * Every transfer creates two transactions: TRANSFER_OUT for the sender and TRANSFER_IN for the receiver.
 *
 * @author Karena Litvinova
 */
@Service
public class TransferService {

    private static final Logger log = LoggerFactory.getLogger(TransferService.class);
    private static final int MAX_NOTE_LENGTH = 140;

    private final AccountService accountService;

    // history of every account, key = account id
    private final Map<Long, List<Transaction>> history = new ConcurrentHashMap<>();
    private final AtomicLong transactionId = new AtomicLong(1);

    public TransferService(AccountService accountService) {
        this.accountService = accountService;
    }

    /**
     * Moves money between two accounts.
     *
     * @return the outgoing transaction of the sender
     */
    public Transaction transfer(Long fromId, Long toId, BigDecimal amount, String note) {
        validate(fromId, toId, amount, note);

        Account from = accountService.getAccountById(fromId);
        Account to = accountService.getAccountById(toId);
        BigDecimal money = amount.setScale(2); // 30.5 -> 30.50, more than 2 decimals is rejected in validate()
        String cleanNote = (note == null || note.isBlank()) ? null : note.trim();

        // Both accounts are locked, always the one with the smaller id first.
        // If A->B and B->A run at the same time they lock in the same order, so there is no deadlock.
        Account firstLock = from.getId() < to.getId() ? from : to;
        Account secondLock = (firstLock == from) ? to : from;

        synchronized (firstLock) {
            synchronized (secondLock) {
                if (from.getBalance().compareTo(money) < 0) {
                    throw new InsufficientFundsException(from.getId(), from.getBalance(), money);
                }

                from.setBalance(from.getBalance().subtract(money));
                to.setBalance(to.getBalance().add(money));

                Instant now = Instant.now();
                Transaction outgoing = new Transaction(transactionId.getAndIncrement(), from.getId(),
                        TransactionType.TRANSFER_OUT, money, now, cleanNote);
                Transaction incoming = new Transaction(transactionId.getAndIncrement(), to.getId(),
                        TransactionType.TRANSFER_IN, money, now, cleanNote);
                addToHistory(outgoing);
                addToHistory(incoming);

                log.info("Transferred {} from account {} to account {}", money, from.getId(), to.getId());
                return outgoing;
            }
        }
    }

    /**
     * Same as transfer(), but accounts come as text from TransferRequestDTO:
     * either the account id ("1") or the IBAN ("LT60...").
     */
    public Transaction transfer(String fromAccount, String toAccount, BigDecimal amount, String note) {
        Account from = findAccount(fromAccount);
        Account to = findAccount(toAccount);
        return transfer(from.getId(), to.getId(), amount, note);
    }

    /**
     * History of one account, newest first.
     */
    public List<Transaction> getTransactions(Long accountId) {
        accountService.getAccountById(accountId); // 404 if the account does not exist

        List<Transaction> result = new ArrayList<>(history.getOrDefault(accountId, List.of()));
        Collections.reverse(result);
        return result;
    }

    private Account findAccount(String account) {
        if (account == null || account.isBlank()) {
            throw new InvalidRequestException("Both accounts are required");
        }
        String value = account.trim();
        if (value.matches("\\d{1,18}")) {
            return accountService.getAccountById(Long.parseLong(value));
        }
        return accountService.getAccountByIban(value);
    }

    private void validate(Long fromId, Long toId, BigDecimal amount, String note) {
        if (fromId == null || toId == null) {
            throw new InvalidRequestException("Both accounts are required");
        }
        if (fromId.equals(toId)) {
            throw new InvalidRequestException("Cannot transfer to the same account");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidRequestException("Amount must be greater than 0");
        }
        if (amount.stripTrailingZeros().scale() > 2) {
            throw new InvalidRequestException("Amount can have at most 2 decimal places");
        }
        if (note != null && note.length() > MAX_NOTE_LENGTH) {
            throw new InvalidRequestException("Note can be at most " + MAX_NOTE_LENGTH + " characters");
        }
    }

    private void addToHistory(Transaction transaction) {
        history.computeIfAbsent(transaction.getAccountId(), id -> new CopyOnWriteArrayList<>())
                .add(transaction);
    }
}
