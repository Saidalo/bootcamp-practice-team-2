package org.demo.banking.application.models;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class Account {
    private long id;
    private String iban;
    private String ownerName;
    private double balance;
}
