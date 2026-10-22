package org.demo.banking.application.services;

import org.demo.banking.application.exceptions.AccountNotFoundException;
import org.demo.banking.application.exceptions.InsufficientFundsException;
import org.demo.banking.application.exceptions.InvalidTransferException;
import org.demo.banking.application.models.Account;
import org.demo.banking.application.models.Transaction;
import org.demo.banking.application.models.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TransferServiceTest {

    private AccountService accountService;
    private TransferService transferService;
    private Account anna;
    private Account peter;

    @BeforeEach
    void setUp() {
        accountService = new AccountService();
        transferService = new TransferService(accountService);
        anna = accountService.createAccount("Anna", new BigDecimal("100.00"));
        peter = accountService.createAccount("Peter", new BigDecimal("10.00"));
    }

    @Test
    void transferMovesMoney() {
        Transaction out = transferService.transfer(anna.getId(), peter.getId(), new BigDecimal("30.50"), "Rent");

        assertEquals(TransactionType.TRANSFER_OUT, out.getType());
        assertEquals(anna.getId(), out.getAccountId());
        assertEquals(new BigDecimal("69.50"), anna.getBalance());
        assertEquals(new BigDecimal("40.50"), peter.getBalance());
    }

    @Test
    void amountIsSavedWithTwoDecimals() {
        Transaction out = transferService.transfer(anna.getId(), peter.getId(), new BigDecimal("5"), null);

        assertEquals("5.00", out.getAmount().toPlainString());
    }

    @Test
    void bothAccountsGetATransaction() {
        transferService.transfer(anna.getId(), peter.getId(), new BigDecimal("5"), "  Coffee  ");

        Transaction out = transferService.getTransactions(anna.getId()).get(0);
        Transaction in = transferService.getTransactions(peter.getId()).get(0);

        assertEquals(TransactionType.TRANSFER_OUT, out.getType());
        assertEquals(TransactionType.TRANSFER_IN, in.getType());
        assertEquals(out.getAmount(), in.getAmount());
        assertEquals(out.getCreatedAt(), in.getCreatedAt());
        assertEquals("Coffee", in.getNote());
    }

    @Test
    void blankNoteIsSavedAsNull() {
        Transaction out = transferService.transfer(anna.getId(), peter.getId(), BigDecimal.ONE, "   ");

        assertNull(out.getNote());
    }

    @Test
    void historyIsNewestFirst() {
        transferService.transfer(anna.getId(), peter.getId(), new BigDecimal("1"), "first");
        transferService.transfer(anna.getId(), peter.getId(), new BigDecimal("2"), "second");

        List<Transaction> history = transferService.getTransactions(anna.getId());

        assertEquals(2, history.size());
        assertEquals("second", history.get(0).getNote());
        assertEquals("first", history.get(1).getNote());
    }

    @Test
    void historyOfNewAccountIsEmpty() {
        assertTrue(transferService.getTransactions(anna.getId()).isEmpty());
    }

    @Test
    void wholeBalanceCanBeSent() {
        transferService.transfer(peter.getId(), anna.getId(), new BigDecimal("10.00"), null);

        assertEquals(0, peter.getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void notEnoughMoneyChangesNothing() {
        assertThrows(InsufficientFundsException.class,
                () -> transferService.transfer(peter.getId(), anna.getId(), new BigDecimal("10.01"), null));

        assertEquals(new BigDecimal("10.00"), peter.getBalance());
        assertEquals(new BigDecimal("100.00"), anna.getBalance());
        assertTrue(transferService.getTransactions(peter.getId()).isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0", "-5", "1.005"})
    void wrongAmountIsRejected(String amount) {
        assertThrows(InvalidTransferException.class,
                () -> transferService.transfer(anna.getId(), peter.getId(), new BigDecimal(amount), null));
    }

    @Test
    void missingValuesAreRejected() {
        assertThrows(InvalidTransferException.class,
                () -> transferService.transfer(anna.getId(), peter.getId(), null, null));
        assertThrows(InvalidTransferException.class,
                () -> transferService.transfer(null, peter.getId(), BigDecimal.ONE, null));
    }

    @Test
    void sameAccountIsRejected() {
        assertThrows(InvalidTransferException.class,
                () -> transferService.transfer(anna.getId(), anna.getId(), BigDecimal.ONE, null));
    }

    @Test
    void tooLongNoteIsRejected() {
        String note = "a".repeat(141);

        assertThrows(InvalidTransferException.class,
                () -> transferService.transfer(anna.getId(), peter.getId(), BigDecimal.ONE, note));
    }

    @Test
    void unknownAccountIsRejected() {
        assertThrows(AccountNotFoundException.class,
                () -> transferService.transfer(anna.getId(), 999L, BigDecimal.ONE, null));
        assertThrows(AccountNotFoundException.class,
                () -> transferService.getTransactions(999L));
        assertEquals(new BigDecimal("100.00"), anna.getBalance());
    }

    @Test
    void transferByIbanWorks() {
        transferService.transferByIban(anna.getIban(), peter.getIban().toLowerCase(), new BigDecimal("20"), null);

        assertEquals(new BigDecimal("80.00"), anna.getBalance());
        assertEquals(new BigDecimal("30.00"), peter.getBalance());
    }

    @Test
    void unknownIbanIsRejected() {
        assertThrows(AccountNotFoundException.class,
                () -> transferService.transferByIban(anna.getIban(), "LT0000", BigDecimal.ONE, null));
    }

    // 4 threads send money in both directions at the same time.
    // Money must not appear or disappear, and opposite transfers must not deadlock.
    @Test
    @Timeout(10)
    void parallelTransfersKeepTotalMoney() throws InterruptedException {
        Account a = accountService.createAccount("A", new BigDecimal("1000.00"));
        Account b = accountService.createAccount("B", new BigDecimal("1000.00"));
        int threads = 4;
        int transfersPerThread = 250;

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        for (int t = 0; t < threads; t++) {
            boolean aToB = t % 2 == 0;
            pool.submit(() -> {
                start.await();
                for (int i = 0; i < transfersPerThread; i++) {
                    if (aToB) {
                        transferService.transfer(a.getId(), b.getId(), BigDecimal.ONE, null);
                    } else {
                        transferService.transfer(b.getId(), a.getId(), BigDecimal.ONE, null);
                    }
                }
                return null;
            });
        }
        start.countDown();
        pool.shutdown();
        assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS));

        assertEquals(0, a.getBalance().add(b.getBalance()).compareTo(new BigDecimal("2000.00")));
        assertEquals(threads * transfersPerThread, transferService.getTransactions(a.getId()).size());
    }
}