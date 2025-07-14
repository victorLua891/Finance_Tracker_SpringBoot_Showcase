
package com.financetracker.finance_tracker_web.config;

import com.financetracker.finance_tracker_web.common.security.jwt.AuthEntryPointJwt;
import com.financetracker.finance_tracker_web.common.security.jwt.AuthTokenFilter;
import com.financetracker.finance_tracker_web.common.service.UserDetailsServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity // Enable annotation-based security (e.g., @PreAuthorize)
public class SecurityConfig {

    private static final Logger logger = LoggerFactory.getLogger(SecurityConfig.class);

    private final UserDetailsServiceImpl userDetailsService;
    private final AuthEntryPointJwt unauthorizedHandler;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, AuthEntryPointJwt unauthorizedHandler) {
        this.userDetailsService = userDetailsService;
        this.unauthorizedHandler = unauthorizedHandler;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        logger.debug("Creating BCryptPasswordEncoder bean.");
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthTokenFilter authenticationJwtTokenFilter() {
        logger.debug("Creating AuthTokenFilter bean.");
        return new AuthTokenFilter();
    }

    @Bean
    public AuthenticationManager authenticationManager(UserDetailsServiceImpl userDetailsService, PasswordEncoder passwordEncoder) {
        logger.debug("Creating AuthenticationManager bean.");
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        logger.debug("Configuring SecurityFilterChain for stateless JWT authentication.");
        http
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exception -> exception.authenticationEntryPoint(unauthorizedHandler))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Public endpoints (no authentication required)
                        .requestMatchers(new AntPathRequestMatcher("/api/auth/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/register")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/admin/login")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/css/**")).permitAll()

                        .requestMatchers(new AntPathRequestMatcher("/js/user/**")).permitAll()

                        .requestMatchers(new AntPathRequestMatcher("/js/admin/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/js/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/images/**")).permitAll()
                        .requestMatchers(new AntPathRequestMatcher("/")).permitAll()

                        // Protected Admin/Master Admin UI paths (served by @Controller)
                        .requestMatchers(new AntPathRequestMatcher("/admin/**")).hasAnyRole("ADMIN", "MASTER_ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/master-admin/**")).hasRole("MASTER_ADMIN")

                        // Protected API paths (served by @RestController)
                        .requestMatchers(new AntPathRequestMatcher("/api/admin/**")).hasAnyRole("ADMIN", "MASTER_ADMIN")
                        .requestMatchers(new AntPathRequestMatcher("/api/master-admin/**")).hasRole("MASTER_ADMIN")

                        .requestMatchers(new AntPathRequestMatcher("/.well-known/**")).permitAll()

                        .requestMatchers(new AntPathRequestMatcher("/api/auth/logout")).permitAll() // Allow logout endpoint

                        // All other requests require authentication
                        .anyRequest().authenticated()
                );

        http.addFilterBefore(authenticationJwtTokenFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}

