package com.financetracker.finance_tracker_web.financial_tracker.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AccountCreationRequest {
    private String name;
    private BigDecimal balance;
    private String currency; // e.g., "USD", "MYR"
}
