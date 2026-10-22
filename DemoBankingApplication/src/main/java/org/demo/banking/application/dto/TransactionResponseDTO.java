package org.demo.banking.application.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionResponseDTO {
    private Long id;
    private Long accountId;
    private String type;
    private BigDecimal amount;
    private Instant createdAt;
    private String note;
}
