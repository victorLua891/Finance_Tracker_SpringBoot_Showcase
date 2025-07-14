package com.financetracker.finance_tracker_web.user.controller;
import com.financetracker.finance_tracker_web.financial_tracker.model.Account;
import com.financetracker.finance_tracker_web.financial_tracker.service.AccountService;
import com.financetracker.finance_tracker_web.common.service.UserService; // Assuming you have a UserService for user details
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * NOTE: The methods in controller although some may be reusable for admin roles,
 *  the
 * */

@RestController
@RequestMapping("/api/accounts")
@PreAuthorize("hasRole('USER')") //NEW
public class UserAccountController {

    private static final Logger logger = LoggerFactory.getLogger(UserAccountController.class);

    private final AccountService accountService;
    private final UserService userService; // To get the currently authenticated user's ID

    @Autowired
    public UserAccountController(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getPrincipal().equals("anonymousUser")) {
            // Assuming your UserDetails implementation returns the username as principal
            String username = authentication.getName();
            // You'll need a method in your UserService to get User ID by username
            return userService.getUserIdByUsername(username); // Implement this in your UserService
        }
        throw new IllegalStateException("User not authenticated.");
    }

    @GetMapping
    public ResponseEntity<List<Account>> getUserAccounts() {
        try {
            Long userId = getCurrentUserId();
            List<Account> accounts = accountService.getAccountsByUserId(userId);
            return ResponseEntity.ok(accounts);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/{accountId}")
    public ResponseEntity<Account> getAccountById(@PathVariable Long accountId) {
        try {
            Long userId = getCurrentUserId();
            Optional<Account> account = accountService.getAccountByIdAndUserId(accountId, userId); //getting the "ONE" specific bank_account of the particular user.
            return account.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    //NEW: only admin should have this remove this out
    // what you did here is not dangerous cause link is not declared like getMapping but if accessed elsewhere then it might be an issue.
    // how to check if still works? If after moving it out,
    // does the loading of the accounts onto the page still works.
    /*@PostMapping
    public ResponseEntity<Account> createAccount(@RequestBody Map<String, Object> accountDetails) {
        try {
            Long userId = getCurrentUserId();
            String name = (String) accountDetails.get("name");
            BigDecimal balance = new BigDecimal(accountDetails.get("balance").toString());
            String accountNumberMasked = (String) accountDetails.get("accountNumberMasked");
            String currency = (String) accountDetails.get("currency");

            Account newAccount = accountService.createAccount(userId, name, balance, currency);
            return ResponseEntity.status(HttpStatus.CREATED).body(newAccount);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // Or a more specific error response
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }*/

    /**
     * An API Endpoint to allow a logged-in user to update the details of
     * one of their own financial accounts.
     */
    //NOTE: Currently, Not Used...
    /*@PutMapping("/{accountId}")
    public ResponseEntity<Account> updateAccount(@PathVariable Long accountId, @RequestBody Map<String, Object> accountDetails) {
        try {
            Long userId = getCurrentUserId();
            String name = (String) accountDetails.get("name");
            BigDecimal balance = new BigDecimal(accountDetails.get("balance").toString());
            String accountNumberMasked = (String) accountDetails.get("accountNumberMasked");
            String currency = (String) accountDetails.get("currency");

            Account updatedAccount = accountService.updateAccount(accountId, userId, name, balance, currency);
            return ResponseEntity.ok(updatedAccount);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // Account not found for user
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }*/

    //NEW: This should not be here. remove ONLY available to admins.
    //  DANGER: This is dangerous cause there is a api declared.
    /*@DeleteMapping("/{accountId}")
    public ResponseEntity<Void> deleteAccount(@PathVariable Long accountId) {
        try {
            Long userId = getCurrentUserId();
            accountService.deleteAccount(accountId, userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build(); // Account not found for user
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }*/
}
