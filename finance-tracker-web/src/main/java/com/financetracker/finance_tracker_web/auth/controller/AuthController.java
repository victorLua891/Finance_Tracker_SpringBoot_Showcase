// AuthController.java
// ... imports ...

package com.financetracker.finance_tracker_web.auth.controller;

import com.financetracker.finance_tracker_web.auth.DebuggingDaoAuthenticationProvider;
import com.financetracker.finance_tracker_web.auth.payload.request.LoginRequest;
import com.financetracker.finance_tracker_web.auth.payload.response.JwtResponse;
import com.financetracker.finance_tracker_web.auth.payload.response.MessageResponse;
import com.financetracker.finance_tracker_web.common.model.User;
import com.financetracker.finance_tracker_web.common.security.jwt.JwtUtils;
import com.financetracker.finance_tracker_web.common.repository.UserRepository; // Import UserRepository
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException; // Import UsernameNotFoundException
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse; // Import this

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtils jwtUtils;
    private final UserRepository userRepository; // Added this field

    public AuthController(AuthenticationManager authenticationManager, JwtUtils jwtUtils, UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtUtils = jwtUtils;
        this.userRepository = userRepository; // Initialize
    }

    @PostMapping("/login")
    public ResponseEntity<?> authenticateUser(@RequestBody LoginRequest loginRequest, HttpServletResponse response) { // Add HttpServletResponse
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);



        org.springframework.security.core.userdetails.UserDetails userDetails =
                (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
        List<String> roles = userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        logger.info("User {} successfully authenticated. JWT generated.", userDetails.getUsername());
        logger.debug("Generated JWT: {}", jwt);

        // --- Set JWT in an HTTP-only cookie ---
        Cookie jwtCookie = new Cookie("jwtToken", jwt);
        jwtCookie.setPath("/"); // Important: Make cookie available to all paths
        jwtCookie.setHttpOnly(true); // Prevent JavaScript access
        jwtCookie.setSecure(true); // Only send over HTTPS (set to false for localhost HTTP testing, but use true in production)
        jwtCookie.setMaxAge(24 * 60 * 60); // 24 hours (or match your JWT expiry)
        response.addCookie(jwtCookie);
        // --- END ---


        User originalUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + userDetails.getUsername()));

        return ResponseEntity.ok(new JwtResponse(
                jwt,
                originalUser.getId(),
                originalUser.getUsername(),
                roles
        ));
    }

    @PostMapping("/logout") // Or @GetMapping, but POST is generally preferred for logout
    public ResponseEntity<?> logoutUser(HttpServletResponse response) {
        logger.info("User attempting to log out.");
        // Create a new cookie with the same name, but set maxAge to 0 to delete it
        Cookie jwtCookie = new Cookie("jwtToken", ""); // Empty value
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setSecure(false); // Must match creation setting for deletion to work
        jwtCookie.setMaxAge(0); // Set max age to 0 to immediately delete the cookie
        response.addCookie(jwtCookie);

        return ResponseEntity.ok(new MessageResponse("User logged out successfully!"));
    }
}