package com.financetracker.finance_tracker_web.financial_tracker.model;

import com.financetracker.finance_tracker_web.common.model.User;
import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; //PK for Account Table.
    private String name; // e.g., "Savings Account-I", "Checking Account" //account type
    private BigDecimal balance;

    @Column(unique = true, nullable = false)
    private String accountNumberMasked; // e.g., "5343 0000 8888"
    private String currency; // e.g., "RM"

    // Many-to-One relationship with User
    @ManyToOne(fetch = FetchType.LAZY) // Use LAZY loading for performance
    @JoinColumn(name = "user_id", nullable = false) // user_id is the foreign key column in the 'account' table
    private User user;


    //BUDGET START
//    @OneToMany(mappedBy = "account", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Budget> budgets; // Assuming you'll have a Budget entity linked to an Account
    //BUDGET END

    // Default Constructor
    public Account() {
    }

    // Constructor with fields (excluding User and Budgets for initial creation)
    public Account(String name, BigDecimal balance, String accountNumberMasked, String currency, User user) {
        this.name = name;
        this.balance = balance;
        this.accountNumberMasked = accountNumberMasked;
        this.currency = currency;
        this.user = user;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getAccountNumberMasked() {
        return accountNumberMasked;
    }

    public void setAccountNumberMasked(String accountNumberMasked) {
        this.accountNumberMasked = accountNumberMasked;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

}
