package com.financetracker.finance_tracker_web.auth.payload.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor // Generates a constructor with all fields (String message)
@Getter
@Setter
public class MessageResponse {
    private String message;
    // No need for a manual constructor if @AllArgsConstructor is used and working
}