package com.financetracker.finance_tracker_web.admin.controller;

import com.financetracker.finance_tracker_web.financial_tracker.dto.AccountCreationRequest;
import com.financetracker.finance_tracker_web.financial_tracker.service.AccountService;
import com.financetracker.finance_tracker_web.financial_tracker.model.Account; // Corrected import path
import com.financetracker.finance_tracker_web.common.model.User; // Assuming common.model.User
import com.financetracker.finance_tracker_web.common.service.UserService; // Assuming common.service.UserService
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // REQUIRED for security
import org.springframework.web.bind.annotation.*; // Import all necessary annotations

import java.util.List;
import java.util.Optional;

@RestController // Marks this class as a REST controller
@RequestMapping("/api/admin/accounts") // Base path for admin-related account operations
@PreAuthorize("hasRole('ADMIN')") // REQUIRED: Only users with the 'ADMIN' role can access this controller
public class AdminAccountController {

    private static final Logger logger = LoggerFactory.getLogger(AdminAccountController.class);

    private final AccountService accountService;
    private final UserService userService; // To get the list of all users

    @Autowired
    public AdminAccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    // Func 01: API to get a list of all users (for the first dropdown)
    @GetMapping("/users") // GET /api/admin/accounts/users
    public ResponseEntity<List<User>> getAllUsers() {
        try {
            List<User> users = userService.getAllUsers();
            return ResponseEntity.ok(users);
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Func 03 Part 1: API to get all accounts for a SPECIFIC user (for the second dropdown)
    // This replaces your commented-out getUserAccounts logic
    @GetMapping("/user/{targetUserId}") // GET /api/admin/accounts/user/{targetUserId}
    public ResponseEntity<List<Account>> getAccountsForSpecificUser(@PathVariable Long targetUserId) {
        try {
            // No need for getCurrentUserId() here; admin specifies the target user
            List<Account> accounts = accountService.getAccountsByUserId(targetUserId);
            if (accounts.isEmpty()) {
                return ResponseEntity.notFound().build(); // Or an empty list depending on desired behavior
            }
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // API to get any single account by its ID (admin can view any account directly)
    // This replaces your commented-out getAccountById logic
    @GetMapping("/{accountId}") // GET /api/admin/accounts/{accountId}
    public ResponseEntity<Account> getAnyAccountById(@PathVariable Long accountId) {
        try {
            // No userId filter needed here, admin can view any account by its ID
            Optional<Account> account = accountService.getAnyAccountById(accountId);
            return account.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Optionally, if you want an admin to see a list of ALL accounts in the system
    @GetMapping("/all") // GET /api/admin/accounts/all
    public ResponseEntity<List<Account>> getAllSystemAccounts() {
        try {
            List<Account> accounts = accountService.getAllAccounts(); // This method would be in AccountService
            return ResponseEntity.ok(accounts);
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Func 04: Create a new account for a specific user
    // POST /api/admin/accounts/user/{targetUserId}
    @PostMapping("/accounts/user/{targetUserId}")
    @ResponseStatus(HttpStatus.CREATED) // Indicate successful creation
    public ResponseEntity<Account> createAccountForUser(
            @PathVariable Long targetUserId,
            @RequestBody AccountCreationRequest request) { // DTO for account details
        logger.info("Admin creating account for user ID: {}", targetUserId);
        try {
            Account newAccount = accountService.createAccount(
                    targetUserId,
                    request.getName(),
                    request.getBalance(),
                    request.getCurrency()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(newAccount);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating account for user {}: {}", targetUserId, e.getMessage());
            return ResponseEntity.badRequest().body(null); // Or return a custom error response
        } catch (Exception e) {
            logger.error("Unexpected error creating account for user {}: {}", targetUserId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

}
