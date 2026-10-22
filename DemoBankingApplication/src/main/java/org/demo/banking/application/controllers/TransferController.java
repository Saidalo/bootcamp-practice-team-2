package org.demo.banking.application.controllers;

import jakarta.validation.Valid;
import org.demo.banking.application.dto.DtoMapper;
import org.demo.banking.application.dto.TransactionResponseDTO;
import org.demo.banking.application.dto.TransferRequestDTO;
import org.demo.banking.application.models.Transaction;
import org.demo.banking.application.services.TransferService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Karena Litvinova
 */
@RestController
@RequestMapping("/transfer")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    // POST /transfer - create a transaction
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionResponseDTO transfer(@Valid @RequestBody TransferRequestDTO request) {
        Transaction outgoing = transferService.transfer(request.getFromAccount(), request.getToAccount(),
                request.getAmount(), request.getNote());
        return DtoMapper.toTransactionResponse(outgoing);
    }
}
