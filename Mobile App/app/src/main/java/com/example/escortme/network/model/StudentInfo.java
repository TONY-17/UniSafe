package com.example.escortme.network.model;

public class StudentInfo {
    private String email;
    private String username;

    public void setEmail(String email) {
        this.email = email;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "StudentInfo{" +
                "email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }
}
