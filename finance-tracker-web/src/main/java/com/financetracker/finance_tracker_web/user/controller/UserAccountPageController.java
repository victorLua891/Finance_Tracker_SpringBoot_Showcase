package com.financetracker.finance_tracker_web.user.controller;

import org.springframework.stereotype.Controller; // Import Controller for view rendering
import org.springframework.ui.Model; // Optional, if you need to pass data to the view immediately
import org.springframework.web.bind.annotation.GetMapping;

//NOTE: this is user specific, admin would be /admin/account
@Controller // This controller is for serving HTML views
public class UserAccountPageController {

    @GetMapping("/account") // This specific mapping will serve the 'accounts.html' template
    public String showAccountsPage(Model model){
        // You can add model attributes here if you want to pass initial data to Thymeleaf,
        // but for dynamic data loaded via JS, it's often not strictly necessary.
        return "user/Account"; // This returns the name of your Thymeleaf template: accounts.html
    }
}