package com.financetracker.finance_tracker_web.financial_tracker.model;

import jakarta.persistence.*;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "budget")
public class Budget {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String category;
    private BigDecimal amount;
    private String description;

    @Autowired
    @ManyToOne(fetch = FetchType.LAZY) // Lazy fetching is usually better for ManyToOne
    @JoinColumn(name = "account_id") // This will be the foreign key column in your 'budget' table
    private Account account; // This will hold the associated Account object

    // Default Constructor
    public Budget() {
    }

    // Constructor with fields (optional, for easier object creation)
    public Budget(String name, String category, BigDecimal amount,
                  String description, Account account) {
        this.name = name;
        this.category = category;
        this.amount = amount;
        this.description = description;
        this.account = account;
    }
}
