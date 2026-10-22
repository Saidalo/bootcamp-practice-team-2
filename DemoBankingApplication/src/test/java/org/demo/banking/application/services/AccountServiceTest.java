package org.demo.banking.application.services;

import org.demo.banking.application.dto.AccountRequestDTO;
import org.demo.banking.application.exception.AccountNotFoundException;
import org.demo.banking.application.exception.IbanAlreadyExistsException;
import org.demo.banking.application.exception.InvalidRequestException;
import org.demo.banking.application.models.Account;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AccountServiceTest {

    private final AccountService service = new AccountService();

    @Test
    void createFromDto() {
        AccountRequestDTO request = new AccountRequestDTO();
        request.setOwnerName(" Anna ");
        request.setIban("lt60 1010 0123 4567 8901");
        request.setInitialBalance(new BigDecimal("50.00"));

        Account account = service.createAccount(request);

        assertEquals(1, account.getId());
        assertEquals("Anna", account.getOwnerName());
        assertEquals("LT601010012345678901", account.getIban());
        assertEquals(new BigDecimal("50.00"), account.getBalance());
        assertSame(account, service.getAccountById(1L));
    }

    @Test
    void ibanIsGeneratedIfEmpty() {
        Account account = service.createAccount("Anna", null, null);

        assertTrue(account.getIban().startsWith("LT"));
        assertEquals(0, account.getBalance().compareTo(BigDecimal.ZERO));
    }

    @Test
    void findByIbanIgnoresCaseAndSpaces() {
        Account account = service.createAccount("Anna", "LT601010012345678901", null);

        assertSame(account, service.getAccountByIban("lt60 1010 0123 4567 8901"));
    }

    @Test
    void sameIbanTwiceIsRejected() {
        service.createAccount("Anna", "LT601010012345678901", null);

        assertThrows(IbanAlreadyExistsException.class,
                () -> service.createAccount("Peter", "LT601010012345678901", null));
    }

    @Test
    void wrongDataIsRejected() {
        assertThrows(InvalidRequestException.class,
                () -> service.createAccount("Anna", null, new BigDecimal("-1")));
        assertThrows(InvalidRequestException.class,
                () -> service.createAccount("  ", null, null));
    }

    @Test
    void unknownAccountThrowsNotFound() {
        assertThrows(AccountNotFoundException.class, () -> service.getAccountById(42L));
        assertThrows(AccountNotFoundException.class, () -> service.getAccountByIban("LT000"));
    }

    @Test
    void allAccountsAreSortedById() {
        Account first = service.createAccount("Anna", null, null);
        Account second = service.createAccount("Peter", null, null);

        assertEquals(first, service.getAllAccounts().get(0));
        assertEquals(second, service.getAllAccounts().get(1));
    }
}
