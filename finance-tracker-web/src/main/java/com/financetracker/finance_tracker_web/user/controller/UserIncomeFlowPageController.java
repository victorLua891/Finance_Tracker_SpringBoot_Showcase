package com.financetracker.finance_tracker_web.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
//@RequestMapping("/income") // The URL path to access this page
public class UserIncomeFlowPageController {

    @GetMapping("/income")
    public String showIncomePage(Model model) {
        // You can add any model attributes here if needed for the Thymeleaf page
        return "user/Income"; // Refers to Income.html in src/main/resources/templates
    }
}