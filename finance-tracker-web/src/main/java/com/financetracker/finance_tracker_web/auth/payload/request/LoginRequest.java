package com.financetracker.finance_tracker_web.auth.payload.request;

// This DTO will receive the JSON payload sent from your frontend login form
// { "username": "...", "password": "..." }

public class LoginRequest {
    private String username;
    private String password;

    // Default constructor (often needed by JSON deserializers like Jackson)
    public LoginRequest() {
    }

    // Constructor with fields (optional, but good for testing)
    public LoginRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    // Getters and Setters are essential for Spring to bind JSON to this object
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}