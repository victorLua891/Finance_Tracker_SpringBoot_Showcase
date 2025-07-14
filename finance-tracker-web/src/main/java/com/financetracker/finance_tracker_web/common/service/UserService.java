package com.financetracker.finance_tracker_web.common.service;

import com.financetracker.finance_tracker_web.common.enums.UserRole;
import com.financetracker.finance_tracker_web.common.model.User;
import com.financetracker.finance_tracker_web.common.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ... other methods like registerUser, findByUsername, etc.

    public Long getUserIdByUsername(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username); // Assuming findByUsername exists
        return userOptional.map(User::getId).orElse(null); // Or throw an exception if user not found
    }

    // You might also need to find a user by ID
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    // You can add other user-related business logic here (e.g., updateUser, changePassword)



    //NOTE: Methods for Master Admin
    // --- MODIFIED: Delete a user (specifically only ADMIN roles from MasterAdminController) ---

    // START Method for AdminAccountController
    public List<User> getAllUsers() {
        return userRepository.findAll(); // Assuming UserRepository extends JpaRepository or has findAll()
    }

    @Transactional
    public User createAdminUser(String username, String rawPassword) {
        logger.info("UserService: Creating new ADMIN user with username: {}", username);
        if (userRepository.findByUsername(username).isPresent()) {
            logger.warn("UserService: User with username {} already exists.", username);
            throw new IllegalArgumentException("User with username " + username + " already exists.");
        }

        User newUser = new User();
        newUser.setUsername(username);
        newUser.setPassword(passwordEncoder.encode(rawPassword));
        newUser.setRole(UserRole.ADMIN); // <<< FORCED TO ADMIN ROLE
        User savedUser = userRepository.save(newUser);
        logger.info("UserService: ADMIN User {} created successfully with ID: {}", username, savedUser.getId());
        return savedUser;
    }

    @Transactional
    public void deleteAdminUser(Long userId) {
        logger.info("UserService: Attempting to delete ADMIN user with ID: {}", userId);
        Optional<User> userOptional = userRepository.findById(userId);

        if (userOptional.isEmpty()) {
            logger.warn("UserService: User with ID {} not found for deletion.", userId);
            throw new IllegalArgumentException("User with ID " + userId + " not found.");
        }

        User userToDelete = userOptional.get();
        if (userToDelete.getRole() != UserRole.ADMIN) { // <<< ENSURE IT'S AN ADMIN
            logger.warn("UserService: User with ID {} is not an ADMIN role, cannot be deleted by this method.", userId);
            throw new IllegalArgumentException("Only ADMIN users can be deleted via this operation.");
        }
        // Prevents a MASTER_ADMIN from accidentally deleting themselves or another MASTER_ADMIN
        if (userToDelete.getRole() == UserRole.MASTER_ADMIN) {
            logger.warn("UserService: Attempt to delete MASTER_ADMIN user with ID {} denied.", userId);
            throw new IllegalArgumentException("Cannot delete MASTER_ADMIN users via this operation.");
        }


        userRepository.deleteById(userId);
        logger.info("UserService: ADMIN User with ID {} deleted successfully.", userId);
    }
    // END Method for AdminAccountController

}
