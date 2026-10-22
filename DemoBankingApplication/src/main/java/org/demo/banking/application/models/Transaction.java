package org.demo.banking.application.models;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class Transaction {
    private long id;
    private long accountId;
    private String type;
    private double amount;
    private Timestamp createdAt;
    private String note;
}
