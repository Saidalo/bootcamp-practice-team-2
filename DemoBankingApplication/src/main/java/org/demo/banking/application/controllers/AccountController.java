package org.demo.banking.application.controllers;

import jakarta.validation.Valid;
import org.demo.banking.application.dto.AccountRequestDTO;
import org.demo.banking.application.dto.AccountResponseDTO;
import org.demo.banking.application.dto.DtoMapper;
import org.demo.banking.application.dto.TransactionResponseDTO;
import org.demo.banking.application.services.AccountService;
import org.demo.banking.application.services.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
public class AccountController {

    private final AccountService accountService;
    private final TransferService transferService;

    public AccountController(AccountService accountService, TransferService transferService) {
        this.accountService = accountService;
        this.transferService = transferService;
    }

    // POST /account - create an account
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponseDTO create(@Valid @RequestBody AccountRequestDTO request) {
        return DtoMapper.toAccountResponse(accountService.createAccount(request));
    }

    // GET /account - list all accounts
    @GetMapping
    public List<AccountResponseDTO> getAll() {
        return accountService.getAllAccounts().stream()
                .map(DtoMapper::toAccountResponse)
                .toList();
    }

    // GET /account/{id} - get by id
    @GetMapping("/{id}")
    public AccountResponseDTO getById(@PathVariable("id") Long id) {
        return DtoMapper.toAccountResponse(accountService.getAccountById(id));
    }

    // GET /account/{id}/transactions - list
    @GetMapping("/{id}/transactions")
    public List<TransactionResponseDTO> getTransactions(@PathVariable("id") Long id) {
        return transferService.getTransactions(id).stream()
                .map(DtoMapper::toTransactionResponse)
                .toList();
    }
}
