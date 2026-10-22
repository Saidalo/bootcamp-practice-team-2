package org.demo.banking.application.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountRequestDTO {
    @NotBlank
    private String iban;

    @NotBlank
    private String ownerName;

    @NotNull
    @PositiveOrZero
    private BigDecimal initialBalance;
}
