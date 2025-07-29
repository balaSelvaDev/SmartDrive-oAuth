package com.phegondev.auth2Peoject.DTO;

import com.phegondev.auth2Peoject.enums.AuthProvider;

public class LoginRequestDTO {

    private String emailId;
    private String password;
    private AuthProvider authProvider = AuthProvider.LOCAL;

    public AuthProvider getAuthProvider() {
        return authProvider;
    }

    public void setAuthProvider(AuthProvider authProvider) {
        this.authProvider = authProvider;
    }
// --- Getters and Setters ---

    public String getEmailId() {
        return emailId;
    }

    public void setEmailId(String emailId) {
        this.emailId = emailId;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
