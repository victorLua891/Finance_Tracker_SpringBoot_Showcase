package com.financetracker.finance_tracker_web.user.controller;

import com.financetracker.finance_tracker_web.financial_tracker.model.Transaction;
import com.financetracker.finance_tracker_web.financial_tracker.enums.TransactionType;
import com.financetracker.finance_tracker_web.financial_tracker.service.TransactionService;
import com.financetracker.finance_tracker_web.common.service.UserService; // To get current user ID
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/transactions") // A general endpoint for all transactions
public class UserTransactionController {

    private static final Logger logger = LoggerFactory.getLogger(UserTransactionController.class);

    private final TransactionService transactionService;
    private final UserService userService;

    @Autowired
    public UserTransactionController(TransactionService transactionService, UserService userService) {
        this.transactionService = transactionService;
        this.userService = userService;
    }

    // Helper method to get the current authenticated user's ID (reused from AccountController)
    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            String username = authentication.getName();
            Long userId = userService.getUserIdByUsername(username);
            if (userId == null) {
                logger.error("TransactionController: UserService returned null userId for username: {}. User ID cannot be determined.", username);
                throw new IllegalStateException("User ID could not be retrieved.");
            }
            return userId;
        }
        logger.warn("TransactionController: User not authenticated or is anonymous. Authentication: {}", authentication);
        throw new IllegalStateException("User not authenticated.");
    }

    //NOTE: this is used behind the scenes when the income & expenditure transactions occur.
    @PostMapping // Handles both income and expenditure based on 'type' in request body
    public ResponseEntity<Transaction> recordTransaction(@RequestBody Map<String, Object> transactionDetails) {
        logger.info("Victor personal");

        try {
            Long userId = getCurrentUserId();
            Long accountId = Long.parseLong(transactionDetails.get("accountId").toString());
            BigDecimal amount = new BigDecimal(transactionDetails.get("amount").toString());
            TransactionType type = TransactionType.valueOf(((String) transactionDetails.get("type")).toUpperCase());
            String description = (String) transactionDetails.get("description");
            LocalDateTime transactionDate = LocalDateTime.parse((String) transactionDetails.get("transactionDate")); // Assuming ISO format from frontend

//            Print error here.
            logger.info("In TransactionController");
            logger.info("Received transaction details:");
            logger.info("  User ID: {}", userId);
            logger.info("  Account ID: {}", accountId);
            logger.info("  Amount: {}", amount);
            logger.info("  Type: {}", type);
            logger.info("  Description: {}", description);
            logger.info("  Transaction Date: {}", transactionDate);

            Transaction newTransaction = transactionService.recordTransaction(userId, accountId, amount, type, description, transactionDate);

            logger.info("newTransaction: {}", newTransaction);
            return ResponseEntity.status(HttpStatus.CREATED).body(newTransaction);

        } catch (IllegalStateException e) {
            logger.error("TransactionController: Unauthorized attempt to record transaction: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException | NullPointerException | DateTimeParseException e) {
            logger.error("TransactionController: Invalid transaction details provided: {}", e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("TransactionController: Internal Server Error recording transaction.", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    //NOTE: both Income & Expenditure uses this
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionsForAccount(@PathVariable Long accountId) {
        try {
            Long userId = getCurrentUserId();
            List<Transaction> transactions = transactionService.getTransactionsByAccountIdAndUserId(accountId, userId);
            return ResponseEntity.ok(transactions);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // Account not found for user
        } catch (Exception e) {
            logger.error("TransactionController: Error fetching transactions for account {}.", accountId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // You can add more endpoints for specific income/expenditure, getting single transaction, update/delete if needed
}