//NEW! 1.0
//src/main/java/com/yourcompany/yourappname/controller/RegistrationController.java
package com.financetracker.finance_tracker_web.user.controller;

import com.financetracker.finance_tracker_web.common.enums.UserRole;
import com.financetracker.finance_tracker_web.common.model.User;
import com.financetracker.finance_tracker_web.common.repository.UserRepository;
import com.financetracker.finance_tracker_web.financial_tracker.service.AccountService; //NEW 1.0 - 11/5/25
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/register")
public class UserRegistrationController {


    private static final Logger logger = LoggerFactory.getLogger(UserRegistrationController.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AccountService accountService;

    //NOTE: password encoder is from securityconfig.
    public UserRegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder, AccountService accountService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.accountService = accountService;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        logger.debug("Serving registration form.");
        model.addAttribute("user", new User()); // Add an empty User object to bind form data
        return "user/register"; // This will resolve to src/main/resources/templates/register.html
    }

    /**
     * RegisterUser
     * responsible for creating a new user
     * also responsible for creating bank accounts for the newly registered user.
     *
     * Method Pseudocode:
     * 1. check if user is already created or not
     * 2. If not created:
     * CRUCIAL: @ModelAttribute is used to bind HTTP request parameters to a JAVA object.
     * a. Encode the password; get the data from form via user parameter, passed into function.
     * b.
     * -
     * c.
     *
     * Some Extra tools:
     * 1. userRepository.sqlmethod to get data; userRepository can be called as per import.
     * 2. @ModelAttribute is a spring MVC annotation:
     * - It is used to bind HTTP request parameters to a JAVA object.
     *
     * */

    @PostMapping
    public String registerUser(@ModelAttribute("user") User user, RedirectAttributes redirectAttributes) {
        logger.info("Received registration request for username: {} {}", user.getUsername(), user.getPassword());

        if (userRepository.existsByUsername(user.getUsername())) {
            logger.warn("Registration failed: Username '{}' already exists.", user.getUsername());
            redirectAttributes.addFlashAttribute("errorMessage", "Username already exists. Please choose a different one.");
            return "redirect:/register"; // Redirect back to the registration page with an error
        }

        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);

        //3. Set User Role:
        user.setRole(UserRole.USER); //NEW

        //2. Save the user entity to the database via UserRepository
        User savedUser = userRepository.save(user);

        try {
            // Define your default accounts here
            // Example: A savings account
            accountService.createAccount(
                    savedUser.getId(),
                    "Main Savings Account",
                    new BigDecimal("1000.00"), // Initial balance
//                    "**** **** **** 3333",     // Masked account number
                    "RM"                       // Currency
            );
            logger.info("Default 'Main Savings Account' created for user: {}", savedUser.getUsername());

            // Example: A checking account
            accountService.createAccount(
                    savedUser.getId(),
                    "Daily Checking Account",
                    new BigDecimal("500.00"),  // Initial balance
//                    "**** **** **** 2222",     // Masked account number
                    "RM"
            );
            logger.info("Default 'Daily Checking Account' created for user: {}", savedUser.getUsername());

        } catch (IllegalArgumentException e) {
            logger.error("Error creating default accounts for user {}: {}", savedUser.getUsername(), e.getMessage());
            // Optionally, you might want to handle this more gracefully,
            // e.g., redirect to an error page or show a partial success message.
            // For now, it will just log the error and proceed to login.
        } catch (Exception e) {
            logger.error("Unexpected error creating default accounts for user {}: {}", savedUser.getUsername(), e.getMessage(), e);
        }

        logger.info("User '{}' registered successfully.", user.getUsername());
        redirectAttributes.addFlashAttribute("successMessage", "Registration successful! You can now log in.");
        return "redirect:/login"; // Redirect to the login page after successful registration
    }
}