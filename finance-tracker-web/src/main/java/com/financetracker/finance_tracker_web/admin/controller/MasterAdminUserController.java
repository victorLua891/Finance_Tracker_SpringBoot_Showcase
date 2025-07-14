package com.financetracker.finance_tracker_web.admin.controller;

import com.financetracker.finance_tracker_web.common.model.User;
import com.financetracker.finance_tracker_web.common.service.UserService;
import com.financetracker.finance_tracker_web.admin.dto.MasterAdminUserCreationRequest; // New DTO
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/master-admin/users") // Distinct base path for MASTER_ADMIN user ops
@PreAuthorize("hasRole('MASTER_ADMIN')")
public class MasterAdminUserController {
    private static final Logger logger = LoggerFactory.getLogger(MasterAdminUserController.class);

    private final UserService userService;

    @Autowired
    public MasterAdminUserController(UserService userService) {
        this.userService = userService;
    }

    // --- MASTER_ADMIN ONLY: Create a new ADMIN user ---
    // POST /api/master-admin/users/admin
    @PostMapping("/admin") // Specific endpoint to create an ADMIN
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<User> createAdminUser(@RequestBody MasterAdminUserCreationRequest request) {
        logger.info("MASTER_ADMIN creating a new ADMIN user: {}", request.getUsername());
        try {
            User newAdminUser = userService.createAdminUser(
                    request.getUsername(),
                    request.getPassword()
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(newAdminUser);
        } catch (IllegalArgumentException e) {
            logger.error("Error creating ADMIN user {}: {}", request.getUsername(), e.getMessage());
            return ResponseEntity.badRequest().body(null);
        } catch (Exception e) {
            logger.error("Unexpected error creating ADMIN user {}: {}", request.getUsername(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    // --- MASTER_ADMIN ONLY: Delete an ADMIN user ---
    // DELETE /api/master-admin/users/admin/{userId}
    @DeleteMapping("/admin/{userId}") // Specific endpoint to delete an ADMIN
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteAdminUser(@PathVariable Long userId) {
        logger.warn("MASTER_ADMIN attempting to delete ADMIN user with ID: {}", userId);
        try {
            userService.deleteAdminUser(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            logger.error("Error deleting ADMIN user {}: {}", userId, e.getMessage());
            // If user not found, or not an ADMIN, return 404/400
            return ResponseEntity.badRequest().build(); // Or more specific 404/403
        } catch (Exception e) {
            logger.error("Unexpected error deleting ADMIN user {}: {}", userId, e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
