package com.example.escortme.network.model;

import java.util.Set;

public class RegisterResponse {
    private String username;
    private String email;
    private String phoneNumber;

    private Set<String> userType;
    private String password;

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Set<String> getUserType() {
        return userType;
    }

    public void setUserType(Set<String> userType) {
        this.userType = userType;
    }
}
