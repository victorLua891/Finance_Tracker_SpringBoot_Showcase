package com.financetracker.finance_tracker_web.financial_tracker.model;

import com.financetracker.finance_tracker_web.financial_tracker.enums.TransactionType;
import jakarta.persistence.*;
import lombok.Data; // For getters, setters, toString, equals, hashCode
import lombok.NoArgsConstructor; // For a no-args constructor

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data // Provides getters, setters, toString, equals, hashCode
@NoArgsConstructor // Provides a no-argument constructor, needed by JPA
@Entity
@Table(name = "transaction") // 'transaction' is a good, clear table name
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private BigDecimal amount; // The amount of money involved in the transaction

    @Enumerated(EnumType.STRING) // Store enum as String in DB ('INCOME', 'EXPENDITURE')
    @Column(nullable = false)
    private TransactionType type; // Enum to distinguish INCOME vs. EXPENDITURE

    private String description; // Optional: e.g., "Monthly Salary", "Groceries"

    @Column(nullable = false)
    private LocalDateTime transactionDate; // When the transaction occurred

    // Many-to-One relationship with Account
    @ManyToOne(fetch = FetchType.LAZY) // Use LAZY loading for performance
    @JoinColumn(name = "account_id", nullable = false) // Foreign key to the account table
    private Account account;

    // Optional: Many-to-One relationship with Category (for future expansion)
    // You would create a 'Category' entity and table for this
    // @ManyToOne(fetch = FetchType.LAZY)
    // @JoinColumn(name = "category_id")
    // private Category category;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // PrePersist and PreUpdate for automatic timestamp management
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // Constructor for creating new transactions
    public Transaction(BigDecimal amount, TransactionType type, String description, LocalDateTime transactionDate, Account account) {
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
        this.account = account;
    }
}
