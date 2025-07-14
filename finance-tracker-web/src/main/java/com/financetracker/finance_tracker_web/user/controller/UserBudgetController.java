package com.financetracker.finance_tracker_web.user.controller;

import com.financetracker.finance_tracker_web.financial_tracker.model.Account;
import com.financetracker.finance_tracker_web.financial_tracker.repository.AccountRepository;
import com.financetracker.finance_tracker_web.financial_tracker.model.Budget;
import com.financetracker.finance_tracker_web.financial_tracker.service.BudgetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
@RequestMapping("/budget")
public class UserBudgetController {

//    @Autowired
    private final BudgetService budgetService;
    private final AccountRepository accountRepository;

    @Autowired
    public UserBudgetController(BudgetService budgetService, AccountRepository accountRepository
    ) {
        this.budgetService = budgetService;
        this.accountRepository = accountRepository;
    }

    // This is the budget class under the previous budget.
    @GetMapping
    public String budgetPage(Model model) {
        // Dummy data for chart (replace with your actual data retrieval logic later)
        model.addAttribute("thisMonthExpenditure", 2500.00);
        model.addAttribute("thisMonthBudgetPlanForExpenditure", 5000.00);

        // Fetch all accounts to populate the dropdown in the modal form
        List<Account> accounts = accountRepository.findAll();
        model.addAttribute("accounts", accounts);

        // Add an empty Budget object to the model for the form to bind to (for new entries)
        model.addAttribute("budget", new Budget());

        // Also fetch all budgets to display on the page (e.g., in a list below the chart)
        List<Budget> budgets = budgetService.findAllBudgets();
        model.addAttribute("budgets", budgets); // Now your HTML can loop through ${budgets}
        System.out.println("The budget page data have run.");
//        return "budget"; // The name of your Thymeleaf template
        return  "budget";
    }


    //they throw the data through the /add endpoint so the function is not called directly, the /add endpoint calls the addBudget function.
    @PostMapping("/add") // This endpoint handles both adding and updating
    public String addBudget(@ModelAttribute("budget") Budget budget, // Spring binds form fields including 'id' and 'account.id'
                             RedirectAttributes redirectAttributes) {

        //TODO: add acc_id to budget... addBudget is missing.
        //need to find the currently logged in user with its account ...
        //...

        System.out.println("Received Budget Object: " + budget.toString()); // Use toString() for debugging

        try {
            budgetService.addBudget(budget); // Call the unified save method in the service
            redirectAttributes.addFlashAttribute("message", "Budget saved successfully!");
        } catch (RuntimeException e) {
            // Catch more specific exceptions if needed (e.g., AccountNotFoundException)
            redirectAttributes.addFlashAttribute("errorMessage", "Error saving budget: " + e.getMessage());
            e.printStackTrace(); // Print stack trace for debugging
        }
        System.out.println("The budget add function have run.");
        return "redirect:/budget"; // Redirect back to the budget page
    }

    // Endpoint to show the form for editing an existing budget
    @GetMapping("/edit")
    public String editBudget(@RequestParam("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Budget> budgetOptional = budgetService.findBudgetById(id);
        if (budgetOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Budget not found for editing.");
            return "redirect:/budget";
        }

        Budget budgetToEdit = budgetOptional.get();
        model.addAttribute("budget", budgetToEdit); // This populates the form with existing data

        // Re-add accounts and other necessary model attributes for the page
        List<Account> accounts = accountRepository.findAll();
        model.addAttribute("accounts", accounts);
        model.addAttribute("thisMonthExpenditure", 2500.00); // Or fetch real data
        model.addAttribute("thisMonthBudgetPlanForExpenditure", 5000.00); // Or fetch real data
        model.addAttribute("budgets", budgetService.findAllBudgets()); // List all budgets

        // Flag to open the modal via JavaScript
        model.addAttribute("openModal", true);
        model.addAttribute("modalTitleText", "Edit Budget"); // For dynamic modal title
        System.out.println("The budget edit function have run.");
//        return "expenditure";
        return "user/budget";
    }

    // Endpoint to delete a budget
    @PostMapping("/delete") // Assuming you'll have a form or link that POSTs to this with the ID
    public String deleteBudget(@RequestParam("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            budgetService.deleteBudget(id);
            redirectAttributes.addFlashAttribute("message", "Budget deleted successfully!");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting budget: " + e.getMessage());
            e.printStackTrace();
        }
        System.out.println("The budget delete function have run.");
        return "redirect:/budget";
    }

}
