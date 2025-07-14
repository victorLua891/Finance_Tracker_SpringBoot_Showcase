package com.financetracker.finance_tracker_web.financial_tracker.repository;

import com.financetracker.finance_tracker_web.financial_tracker.model.Transaction;
import com.financetracker.finance_tracker_web.financial_tracker.enums.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions for a specific account
    List<Transaction> findByAccountId(Long accountId);

    // Find all transactions for a specific account and type (e.g., all income for an account)
    List<Transaction> findByAccountIdAndType(Long accountId, TransactionType type);

    // You can add more custom query methods as needed for reports, etc.
}
