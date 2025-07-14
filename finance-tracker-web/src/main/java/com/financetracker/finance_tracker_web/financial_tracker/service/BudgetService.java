package com.financetracker.finance_tracker_web.financial_tracker.service;

import com.financetracker.finance_tracker_web.financial_tracker.model.Account;
import com.financetracker.finance_tracker_web.financial_tracker.repository.AccountRepository;
//import com.financetracker.finance_tracker_web.repository.AccountRepository;
import com.financetracker.finance_tracker_web.financial_tracker.model.Budget;
import com.financetracker.finance_tracker_web.financial_tracker.repository.BudgetRepository;

//import com.yourfinancetracker.repository.AccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final AccountRepository accountRepository;

    public BudgetService(BudgetRepository budgetRepository, AccountRepository accountRepository) {
        this.budgetRepository = budgetRepository;
        this.accountRepository = accountRepository;
    }

    // This method replaces the functionality of your old 'addBudget'
    // It handles both adding (inserting) and updating existing budgets.
    @Transactional
    //TODO: Add logic for return failure.
    public Budget addBudget(Budget budget) { // Now accepts a Budget object directly
        //TODO: Get/Retrieve Curr. Acc. ID.

        // Ensure an account is provided
        if (budget.getAccount() == null || budget.getAccount().getId() == null) {
            throw new IllegalArgumentException("Budget must be associated with an Account (Account ID is missing).");
        }

        // Fetch the managed Account entity from the database
        // I set an artificial account (get_acc variable) that I then find the accId from budget.getAccount();
        //FIXME: THIS WILL CRASH, account repository & service not created yet.
        Account account = accountRepository.findById(budget.getAccount().getId())
                .orElseThrow(() -> new RuntimeException("Account not found with ID: " + budget.getAccount().getId()));

        // Set the managed Account entity on the budget object
        budget.setAccount(account);

        // Determine if it's a new budget or an update
        //NOTE: logic to subtract specified budget from account amount.
        // again... acc. repo. & service not created yet.
        if (budget.getId() == null) { // This is a new budget (INSERT)
            // Decrease account balance by the new budget amount
            BigDecimal newBalance = account.getBalance().subtract(budget.getAmount());
            account.setBalance(newBalance);
            accountRepository.save(account); // Save updated account balance
            return budgetRepository.save(budget); // Save new budget
        } else { // This is an existing budget (UPDATE)
            //NOTE: I don't quite understand any of this.
            //Retrieve the old budget to calculate the balance difference
            Budget oldBudget = budgetRepository.findById(budget.getId())
                    .orElseThrow(() -> new RuntimeException("Budget not found with ID: " + budget.getId()));

            // Calculate the difference in amount
            BigDecimal oldAmount = oldBudget.getAmount();
            BigDecimal newAmount = budget.getAmount();
            BigDecimal amountDifference = newAmount.subtract(oldAmount); // Positive if new > old, negative if new < old

            // Update account balance based on the difference
            BigDecimal updatedBalance = account.getBalance().subtract(amountDifference);
            account.setBalance(updatedBalance);
            accountRepository.save(account); // Save updated account balance

            // Save the updated budget
            return budgetRepository.save(budget);
        }
        //TODO: return failed value

    }


    // other methods in this service related to fetching or processing expenditures

    public List<Budget> findAllBudgets() {
        return budgetRepository.findAll();
    }

    public Optional<Budget> findBudgetById(Long id) {
        return budgetRepository.findById(id);
    }

    @Transactional
    public void deleteBudget(Long budgetId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new RuntimeException("Budget not found with ID: " + budgetId));

        Account account = budget.getAccount();
        if (account != null) {
            // Revert the amount to the account balance when deleting
            account.setBalance(account.getBalance().add(budget.getAmount()));
            accountRepository.save(account);
        }
        budgetRepository.delete(budget);
    }

}