package com.financetracker.finance_tracker_web.common.service;

import com.financetracker.finance_tracker_web.common.model.User;
import com.financetracker.finance_tracker_web.common.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//KNOWLEDGE: Part of the thymeleaf-extras-springsecurity6, not starter-security
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList; // For authorities, simplified for this example
import java.util.Collections;
import java.util.List;

/** NOTE: Necessary file for the spring security framework.
 *   the springSecurity would analyze & find the UserDetailsServiceImpl.java file*/

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private static final Logger logger = LoggerFactory.getLogger(UserDetailsServiceImpl.class);

    private final UserRepository userRepository;

    public UserDetailsServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("2ND: I'm from UserDetailsServiceImpl");
        logger.debug("Attempting to load user by username: {}", username);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    logger.warn("User not found: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });

        //NOTE: returns username with password in bcrypt form, but yes returns correct user
        logger.info("3RD: User details from userRepository.findByUsername: {} {}", user.getUsername(), user.getPassword());
        logger.info("User {} found for authentication.", username);

        //NEW
        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + user.getRole().name()) // user.getRole() must return a UserRole enum
        );
        //NEW

        // Here you would typically load user roles/authorities from the database
        // For simplicity, we'll grant a generic "USER" authority.
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities //NEW
                //new ArrayList<>() // Empty list for authorities for now, add proper roles if needed //OLD
        );
    }
}
