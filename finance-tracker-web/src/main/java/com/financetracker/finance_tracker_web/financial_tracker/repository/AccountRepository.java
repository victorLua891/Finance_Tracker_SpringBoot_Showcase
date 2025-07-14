package com.financetracker.finance_tracker_web.financial_tracker.repository; // Correct package


import com.financetracker.finance_tracker_web.financial_tracker.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    // Custom method to find accounts by user ID
    List<Account> findByUserId(Long userId);

    Optional<Account> findByIdAndUserId(Long accountId, Long userId);

    //NEW method to check for unique account numbers
    Optional<Account> findByAccountNumberMasked(String accountNumberMasked);
}
