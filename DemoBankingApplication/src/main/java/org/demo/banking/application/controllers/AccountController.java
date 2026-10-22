package org.demo.banking.application.controllers;

import org.demo.banking.application.dto.AccountRequestDTO;
import org.demo.banking.application.dto.AccountResponseDTO;
import org.demo.banking.application.dto.TransactionResponseDTO;
import org.demo.banking.application.models.Account;
import org.demo.banking.application.services.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // POST /account - create an account
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Account create(@RequestBody AccountRequestDTO request) {
        return accountService.createAccount(request);
    }

    // GET /account/{id} - get by id
    @GetMapping("/{id}")
    public AccountResponseDTO getById(@PathVariable Long id) {
        return accountService.getAccountById(id);
    }

    // GET /account/{id}/transactions - list
    @GetMapping("/{id}/transactions")
    public List<TransactionResponseDTO> getTransactions(@PathVariable Long id) {
        return accountService.getTransactions(id);
    }
}
