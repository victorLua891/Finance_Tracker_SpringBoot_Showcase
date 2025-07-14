package com.financetracker.finance_tracker_web.financial_tracker.repository;

//import com.financetracker.finance_tracker_web.financial_tracker.model.Budget; //redundant import cause in same directory
import com.financetracker.finance_tracker_web.financial_tracker.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
//@RepositoryRestResource //KNOWLEDGE: Only if you are not using controller.
// KNOWLEDGE: Useful for internal application logic (ie: you don't have a front end & you just want a quick backend display.)
public interface BudgetRepository extends JpaRepository<Budget, Long> {
}
