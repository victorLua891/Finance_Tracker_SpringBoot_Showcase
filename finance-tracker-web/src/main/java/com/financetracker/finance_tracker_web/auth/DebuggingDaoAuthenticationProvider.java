package com.financetracker.finance_tracker_web.auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;

// Extension of DaoAuthenticationProvider
// adding logging before its core password comparison.
public class DebuggingDaoAuthenticationProvider extends DaoAuthenticationProvider {

    private static final Logger logger = LoggerFactory.getLogger(DebuggingDaoAuthenticationProvider.class);
    private final PasswordEncoder passwordEncoder; // Keep a reference to the encoder for logging

    // Constructor to inject UserDetailsService and PasswordEncoder
    public DebuggingDaoAuthenticationProvider(UserDetailsService userDetailsService, PasswordEncoder passwordEncoder) {
        super(); // Call the superclass constructor
        this.setUserDetailsService(userDetailsService);
        this.setPasswordEncoder(passwordEncoder);
        this.passwordEncoder = passwordEncoder; // Store reference for logging
    }

    // Override the additionalAuthenticationChecks method to log the password
    @Override
    protected void additionalAuthenticationChecks(UserDetails userDetails,
                                                  UsernamePasswordAuthenticationToken authentication) throws AuthenticationException {

        if (authentication.getCredentials() == null) {
            logger.debug("Failed to authenticate: no credentials provided");
            throw new BadCredentialsException(this.messages.getMessage(
                    "AbstractUserDetailsAuthenticationProvider.badCredentials",
                    "Bad credentials"));
        }

        String rawPassword = authentication.getCredentials().toString(); // This is the plain-text password from the form
        String encodedPasswordFromDb = userDetails.getPassword(); // This is the hashed password from the DB

        // --- DEBUGGING LOG ---
        logger.debug("--- DEBUGGING PASSWORD HASHING DURING LOGIN ---");
        logger.debug("Plain-text password from form (CAUTION: visible in logs!): {}", rawPassword); // TEMPORARY, REMOVE IN PROD!

        // Hash the raw password (from the form) using the same encoder
        // This is what Spring Security will internally compare against the DB hash.
        String hashedPasswordFromForm = passwordEncoder.encode(rawPassword);
        logger.debug("Hashed password (from form input) for comparison: {}", hashedPasswordFromForm);
        logger.debug("Hashed password (retrieved from DB): {}", encodedPasswordFromDb);
        logger.debug("--- END DEBUGGING PASSWORD HASHING DURING LOGIN ---");
        // --- END DEBUGGING LOG ---

        // (This will eventually call passwordEncoder.matches(rawPassword, encodedPasswordFromDb))
        super.additionalAuthenticationChecks(userDetails, authentication);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication));
    }
}