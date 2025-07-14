package com.financetracker.finance_tracker_web.financial_tracker.service;

import com.financetracker.finance_tracker_web.financial_tracker.model.Account;
import com.financetracker.finance_tracker_web.common.model.User;
import com.financetracker.finance_tracker_web.financial_tracker.repository.AccountRepository;
import com.financetracker.finance_tracker_web.common.repository.UserRepository; // Assuming you have a UserRepository
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
public class AccountService {
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class); // <--- ADD THIS LINE

    private final AccountRepository accountRepository;
    private final UserRepository userRepository; // To fetch User entities
    private static final Random RANDOM = new Random(); // Initialize Random for use

    @Autowired
    public AccountService(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    private String generateUniqueAccountNumberMasked() {
        String accountNumber;
        boolean unique = false;
        do {
            // Generate four groups of 4 digits and format them with leading zeros
            String group1 = String.format("%04d", RANDOM.nextInt(10000));
            String group2 = String.format("%04d", RANDOM.nextInt(10000));
            String group3 = String.format("%04d", RANDOM.nextInt(10000));
            String group4 = String.format("%04d", RANDOM.nextInt(10000));

            accountNumber = String.format("%s %s %s %s", group1, group2, group3, group4);

            // Check if this generated number already exists in the database
            if (accountRepository.findByAccountNumberMasked(accountNumber).isEmpty()) {
                unique = true;
            }
            // Optional safety break: Highly unlikely for 10^16 combinations, but good practice.
            // If stuck, log a warning.
        } while (!unique); // Keep generating until a unique one is found
        return accountNumber;
    }

    // Get all accounts for a specific user
    // NOTE: Both user & admin.
    public List<Account> getAccountsByUserId(Long userId) {
        logger.info("AccountService: Receiving request to fetch accounts for userId: {}", userId); // Existing log
        List<Account> accounts = accountRepository.findByUserId(userId);
        logger.info("AccountService: Found {} accounts for userId: {}", accounts.size(), userId); // <--- ADD THIS LINE HER

        return accountRepository.findByUserId(userId);
    }

    // Get a single account by ID (ensure it belongs to the user)
    public Optional<Account> getAccountByIdAndUserId(Long accountId, Long userId) {
        return accountRepository.findByIdAndUserId(accountId, userId);
    }

    //NOTE: should be only available for admins.
    // But this is neccessary cause our users can create bank_accounts during registration.
    @Transactional
    public Account createAccount(Long userId, String name, BigDecimal balance, String currency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User with ID " + userId + " not found."));

        // NEW: Generate the unique masked account number here
        String generatedAccountNumber = generateUniqueAccountNumberMasked();

        // Pass the generated number to the Account constructor
        Account account = new Account(name, balance, generatedAccountNumber, currency, user);
        user.addAccount(account); // Add account to user's list and set user on account
        return accountRepository.save(account);
    }

    //NOTE: uncommented out cause of admin requirement.
    @Transactional
    public Account updateAccount(Long accountId, Long userId, String name, BigDecimal balance, String currency) {
        Optional<Account> existingAccountOptional = accountRepository.findByIdAndUserId(accountId, userId);
        if (existingAccountOptional.isPresent()) {
            Account existingAccount = existingAccountOptional.get();
            existingAccount.setName(name);
            existingAccount.setBalance(balance);
//            existingAccount.setAccountNumberMasked(accountNumberMasked);
            existingAccount.setCurrency(currency);
            return accountRepository.save(existingAccount);
        } else {
            throw new IllegalArgumentException("Account with ID " + accountId + " not found for user ID " + userId);
        }
    }
    // =================== NOTE: ADMIN FUNCTIONALITIES ===================

    //NOTE: uncommented out cause of admin requirement.
    @Transactional
    public void deleteAccount(Long accountId, Long userId) {
        Optional<Account> accountOptional = accountRepository.findByIdAndUserId(accountId, userId);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            User user = account.getUser();
            if (user != null) {
                user.removeAccount(account); // Remove account from user's list
            }
            accountRepository.delete(account);
            logger.info("AccountService: Successfully deleted account {} for userId: {}", accountId, userId);
        } else {
            logger.error("AccountService: Account with ID {} not found for user ID {} for deletion.", accountId, userId);
            throw new IllegalArgumentException("Account with ID " + accountId + " not found for user ID " + userId);
        }
    }

    @Transactional
    public Account updateAccountBalance(Long accountId, Long userId, BigDecimal newBalance) {
        Optional<Account> existingAccountOptional = accountRepository.findByIdAndUserId(accountId, userId);
        if (existingAccountOptional.isPresent()) {
            Account account = existingAccountOptional.get();
            account.setBalance(newBalance);
            return accountRepository.save(account);
        } else {
            throw new IllegalArgumentException("Account with ID " + accountId + " not found for user ID " + userId);
        }
    }

    //NEW - 7/7/25
    // Get all accounts in the system (Primarily for AdminAccountController overview)
    public List<Account> getAllAccounts() {
        logger.info("AccountService: Fetching all accounts in the system (Admin view).");
        return accountRepository.findAll();
    }

    // Get any single account by its ID (Primarily for AdminAccountController direct lookup)
    public Optional<Account> getAnyAccountById(Long accountId) {
        logger.info("AccountService: Fetching any account by ID: {}", accountId);
        return accountRepository.findById(accountId);
    }

    // Delete any account by its ID (ADMIN PRIVILEGE ONLY - Called only from AdminAccountController)
    @Transactional
    public void deleteAnyAccount(Long accountId) {
        logger.info("AccountService: Admin attempting to delete account with ID: {}", accountId);
        Optional<Account> accountOptional = accountRepository.findById(accountId);
        if (accountOptional.isPresent()) {
            Account account = accountOptional.get();
            User user = account.getUser();
            if (user != null) {
                user.removeAccount(account); // Ensure relationship integrity
            }
            accountRepository.delete(account);
            logger.info("AccountService: Admin successfully deleted account with ID: {}", accountId);
        } else {
            logger.error("AccountService: Account with ID {} not found for deletion by admin.", accountId);
            throw new IllegalArgumentException("Account with ID " + accountId + " not found.");
        }
    }
    //NEW - 7/7/25
}
