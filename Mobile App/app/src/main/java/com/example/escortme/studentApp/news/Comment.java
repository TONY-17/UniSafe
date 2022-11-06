package com.example.escortme.studentApp.news;

public class Comment {
    private Long id;
    private String content;
    private String user;
    private String date;

    public Comment(Long id, String content, String user, String date) {
        this.id = id;
        this.content = content;
        this.user = user;
        this.date = date;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
