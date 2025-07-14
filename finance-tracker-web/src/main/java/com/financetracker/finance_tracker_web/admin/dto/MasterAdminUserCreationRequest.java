package com.financetracker.finance_tracker_web.admin.dto;

import lombok.Data;

@Data
public class MasterAdminUserCreationRequest {
    private String username;
    private String password;
}
