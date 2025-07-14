package com.financetracker.finance_tracker_web.financial_tracker.service;

import com.financetracker.finance_tracker_web.common.service.UserService;
import com.financetracker.finance_tracker_web.financial_tracker.model.Account;
import com.financetracker.finance_tracker_web.financial_tracker.model.Transaction;
import com.financetracker.finance_tracker_web.financial_tracker.enums.TransactionType;
import com.financetracker.finance_tracker_web.financial_tracker.repository.TransactionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class TransactionService {

    private static final Logger logger = LoggerFactory.getLogger(TransactionService.class);

    private final TransactionRepository transactionRepository;
    private final AccountService accountService; // Reusing existing AccountService to update balance
    private final UserService userService; // To verify user ownership of account

    @Autowired
    public TransactionService(TransactionRepository transactionRepository, AccountService accountService, UserService userService) {
        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
        this.userService = userService;
    }

    @Transactional
    public Transaction recordTransaction(Long userId, Long accountId, BigDecimal amount, TransactionType type,
                                         String description, LocalDateTime transactionDate) {
        // 1. Verify the account belongs to the current user
        Optional<Account> accountOptional = accountService.getAccountByIdAndUserId(accountId, userId);
        logger.info("1.1");
        if (accountOptional.isEmpty()) {
            logger.info("1.2");
            logger.warn("TransactionService: Account with ID {} not found for user ID {}.", accountId, userId);
            throw new IllegalArgumentException("Account not found or does not belong to the user.");
        }
        Account account = accountOptional.get();


        logger.info("2.1");
        // 2. Validate amount
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            logger.info("2.2");
            throw new IllegalArgumentException("Transaction amount must be positive.");
        }

        // 3. Update account balance based on transaction type
        BigDecimal newBalance;
        logger.info("3.1");
        if (type == TransactionType.INCOME) {
            logger.info("3.2");
            newBalance = account.getBalance().add(amount);
            logger.info("Recording INCOME: Adding {} to account {} (ID: {}). New balance: {}", amount, account.getName(), account.getId(), newBalance);
        } else if (type == TransactionType.EXPENDITURE) {
            logger.info("3.3");
            // Optional: Add logic to prevent negative balance if desired
            if (account.getBalance().compareTo(amount) < 0) {
                logger.warn("Expenditure exceeds current balance for account {} (ID: {}). Current: {}, Attempted: {}", account.getName(), account.getId(), account.getBalance(), amount);
                // throw new IllegalArgumentException("Expenditure cannot exceed account balance.");
                // Or allow negative balance and handle it visually on frontend
            }
            newBalance = account.getBalance().subtract(amount);
            logger.info("Recording EXPENDITURE: Subtracting {} from account {} (ID: {}). New balance: {}", amount, account.getName(), account.getId(), newBalance);
        } else {
            logger.info("3.4");
            throw new IllegalArgumentException("Invalid transaction type: " + type);
        }


        logger.info("4.1");
        // Use the existing accountService method to update the balance
        accountService.updateAccountBalance(account.getId(), userId, newBalance);

        // 4. Create and save the new transaction record
        logger.info("5.1");
        Transaction newTransaction = new Transaction(amount, type, description, transactionDate, account);
        Transaction savedTransaction = transactionRepository.save(newTransaction);
        logger.info("Transaction recorded successfully. ID: {}, Type: {}, Amount: {}", savedTransaction.getId(), savedTransaction.getType(), savedTransaction.getAmount());
        return savedTransaction;
    }

    // Get transactions for a specific account and user
    public List<Transaction> getTransactionsByAccountIdAndUserId(Long accountId, Long userId) {
        // First, verify the account belongs to the user
        Optional<Account> accountOptional = accountService.getAccountByIdAndUserId(accountId, userId);
        if (accountOptional.isEmpty()) {
            throw new IllegalArgumentException("Account not found or does not belong to the user.");
        }
        return transactionRepository.findByAccountId(accountId);
    }

    // Get a specific transaction by ID for a specific user (ensuring ownership)
    public Optional<Transaction> getTransactionByIdAndUserId(Long transactionId, Long userId) {
        Optional<Transaction> transactionOptional = transactionRepository.findById(transactionId);
        if (transactionOptional.isPresent()) {
            Transaction transaction = transactionOptional.get();
            if (!transaction.getAccount().getUser().getId().equals(userId)) {
                logger.warn("Attempt to access transaction {} by unauthorized user {}. Account owner: {}", transactionId, userId, transaction.getAccount().getUser().getId());
                return Optional.empty(); // Transaction does not belong to this user
            }
            return Optional.of(transaction);
        }
        return Optional.empty();
    }
}