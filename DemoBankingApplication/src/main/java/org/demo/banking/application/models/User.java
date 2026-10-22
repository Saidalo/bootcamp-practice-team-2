package org.demo.banking.application.models;

import lombok.Getter;

@Getter
public class User {
    private long id;
    private String userName;
    private String passwordHash;
    private String role;
}
