package com.financetracker.finance_tracker_web.admin.controller;

import com.financetracker.finance_tracker_web.common.model.User;
import com.financetracker.finance_tracker_web.common.service.UserService;
// Removed UserRole import if not directly used in controller
// Removed UserCreationRequest import if not directly used in controller

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus; // Still needed for ResponseEntity
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {
    private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);

    private final UserService userService;

    @Autowired
    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    // Get all users (still accessible by ADMIN and MASTER_ADMIN)
    // GET /api/admin/users
    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        logger.info("Admin accessing all users list.");
        List<User> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // Get a specific user by ID (still accessible by ADMIN and MASTER_ADMIN)
    // GET /api/admin/users/{userId}
    @GetMapping("/{userId}")
    public ResponseEntity<User> getUserById(@PathVariable Long userId) {
        logger.info("Admin retrieving user with ID: {}", userId);
        Optional<User> user = userService.findById(userId);
        return user.map(ResponseEntity::ok)
                .orElseGet(() -> {
                    logger.warn("User with ID {} not found.", userId);
                    return ResponseEntity.notFound().build();
                });
    }
}
