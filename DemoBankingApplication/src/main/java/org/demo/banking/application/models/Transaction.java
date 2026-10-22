package org.demo.banking.application.models;

import lombok.Getter;

import java.sql.Timestamp;

@Getter
public class Transaction {
    private long id;
    private String account;
    private String type;
    private double amount;
    private Timestamp createdAt;
    private String note;
}
