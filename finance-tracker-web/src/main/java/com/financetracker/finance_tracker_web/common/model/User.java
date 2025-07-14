package com.financetracker.finance_tracker_web.common.model;

import com.financetracker.finance_tracker_web.financial_tracker.model.Account;
import com.financetracker.finance_tracker_web.common.enums.UserRole; // NEW
import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

//import javax.persistence.*;

@Data //Also provides toString, equals, builder etc. other than the getter & setters.
@Entity
@Table(name = "users")  // Make sure this matches your PostgreSQL
//@NoArgsConstructor // Lombok for no-arg constructor
//@AllArgsConstructor // Lombok for all-arg constructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING) // Stores the enum name ("USER", "ADMIN", "MASTER_ADMIN") as a string in the DB
    @Column(name = "role", nullable = false) // Set to nullable=true temporarily for DB migration if needed
    private UserRole role;

    // One-to-Many relationship with Account
    // 'mappedBy' indicates the field in the Account entity that owns the relationship (the 'user' field in Account)
    // 'cascade = CascadeType.ALL' means that operations (like persist, merge, remove) on a User will cascade to its associated Account entities.
    // 'orphanRemoval = true' means if an Account is removed from the 'accounts' list of a User, it will be deleted from the database.
    // 'fetch = FetchType.LAZY' means accounts will only be loaded when explicitly accessed.
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Account> accounts = new ArrayList<>(); // Initialize to prevent NullPointerExceptions

    // Constructors
    public User() {}

    public User(String username, String password){
        this.username = username;
        this.password = password;
        // Default role if not provided, or ensure it's set later.
        // For existing users where role will be migrated, this constructor is fine.
        // For new user creation, you'd want to pass the role.
        this.role = UserRole.USER; // Example default if not provided
    }

    public User(String username, String password, UserRole role) {
        this.username = username;
        this.password = password;
        this.role = role;
    }

    // Getters and setters
    // Lombok's @Data annotation will generate the getters and setters for 'id', 'username', 'password', and 'accounts'.

    public void addAccount(Account account) {
        if (accounts == null) { // Defensive check, though initialized above
            accounts = new ArrayList<>();
        }
        accounts.add(account);
        account.setUser(this); // Set the user on the account side
    }

    public void removeAccount(Account account) {
        if (accounts != null) {
            accounts.remove(account);
            account.setUser(null); // Remove the user association on the account side
        }
    }
}

