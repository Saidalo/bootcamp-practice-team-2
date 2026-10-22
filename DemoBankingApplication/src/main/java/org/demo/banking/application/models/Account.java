package org.demo.banking.application.models;

import lombok.Getter;

@Getter
public class Account {
    private long id;
    private String iban;
    private String ownerName;
    private double balance;
}
