package org.demo.banking.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class TransferRequestDTO {
    @NotBlank
    private String fromAccount;

    @NotBlank
    private String toAccount;

    @Positive
    private double amount;
}
