package com.financetracker.finance_tracker_web.admin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.security.access.prepost.PreAuthorize;

@Controller
public class AdminFrontendController {

    // This maps the browser URL /admin/account-management to the HTML template
    @GetMapping("/admin/account-management")
    @PreAuthorize("hasRole('ADMIN')") // Only ADMIN and MASTER_ADMIN roles can access this page
    public String showAdminAccountManagementPage() {
        return "admin/account_management"; // Refers to src/main/resources/templates/admin/account_management.html
    }

    // You'll also need a controller for the Admin Dashboard (e.g., /admin/dashboard)
    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAdminDashboard() {
        return "admin/dashboard"; // Refers to src/main/resources/templates/admin/dashboard.html
    }

    // You'll also need a controller for the Admin User Management page (e.g., /admin/users)
    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    public String showAdminUserManagementPage() {
        return "admin/user_management"; // Refers to src/main/resources/templates/admin/user_management.html
    }

    // Admin login page (as discussed previously)
    @GetMapping("/admin/login")
    public String showAdminLoginPage() {
        return "admin/login"; // Refers to src/main/resources/templates/admin/login.html
    }
}
