package com.financetracker.finance_tracker_web.user.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

//NOTE: admin would have a separate login & home functionality.
// admins would have login and home but the home would be like something else
@Controller
public class UserHomeController {

    private static final Logger logger = LoggerFactory.getLogger(UserHomeController.class);

    @GetMapping("/login")
    public String loginPage(Model model) {
        logger.debug("Accessing login page.");
        // You can add logic here to display error messages based on URL parameters (e.g., ?error=true)
        return "user/login"; // This assumes you have a src/main/resources/templates/login.html
    }

    @GetMapping("/home")
    public String homePage(Model model) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        logger.info("Accessing home page. Current user: {}", currentUserName);
        model.addAttribute("username", currentUserName);
        return "user/home"; // This assumes you have a src/main/resources/templates/home.html
    }

    @GetMapping("/")
    public String rootRedirect() {
        return "redirect:/home";
    }
}
