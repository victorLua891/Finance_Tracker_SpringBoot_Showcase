package com.financetracker.finance_tracker_web.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

//NOTE: User specific. unless... add. func.
//@Controller: "Hey server, I need a whole new page (HTML) to show the user."
//@RestController: "Hey server, I need some data (usually JSON) to update the current page or for the client to display."
//@Controller for the Home page
@Controller
public class UserDashboardController {
    @GetMapping("/dashboard")
    public String home(Model model){
        // 1. Fetch your financial data (replace with your actual data retrieval logic)
        double totalSavings = 1500.00;
        double thisMonthExpenditureValue = 500.00;
        double thisMonthBudgetPlanValue = 1000.00;
        double thisMonthBudgetForExpenditureValue = 700.00; // Example specific budget for expenditure

        // 2. Add the data to the model
        model.addAttribute("totalSavings", totalSavings);
        model.addAttribute("thisMonthExpenditure", thisMonthExpenditureValue);
        model.addAttribute("thisMonthBudgetPlan", thisMonthBudgetPlanValue);
        model.addAttribute("thisMonthBudgetPlanForExpenditure", thisMonthBudgetForExpenditureValue);


        return "user/Dashboard";
    }

}

