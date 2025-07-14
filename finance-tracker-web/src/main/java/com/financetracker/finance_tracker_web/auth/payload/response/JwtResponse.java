package com.financetracker.finance_tracker_web.auth.payload.response;

import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor; // Good to keep for JSON deserialization

@Getter
@Setter
@NoArgsConstructor // Keep this if you need a default constructor (e.g., for JSON deserialization)
public class JwtResponse {
    private String token;
    private Long id;
    private String username;
    private List<String> roles;
    private String type = "Bearer"; // Default value

    // Custom constructor (no @AllArgsConstructor to prevent Lombok from generating a different one)
    public JwtResponse(String accessToken, Long id, String username, List<String> roles) {
        this.token = accessToken;
        this.id = id;
        this.username = username;
        this.roles = roles;
        // 'type' is initialized by the field declaration, or you can explicitly set it here:
        // this.type = "Bearer";
    }
    // other getters/setters are now handled by @Getter and @Setter
}