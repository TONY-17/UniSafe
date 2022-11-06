package com.example.escortme.studentApp.news;

import java.util.List;

public class Alert {

    private Long id;
    private String user;
    private String time;
    private String content;
    private String count;
    private List<String> urls;

    public String getCount() {
        return count;
    }


    public void setCount(String count) {
        this.count = count;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Alert(Long id ,String user, String time, String content, String count, List<String> urls) {
        this.id = id;
        this.user = user;
        this.time = time;
        this.content = content;
        this.count = count;
        this.urls = urls;
    }

    public List<String> getUrls() {
        return urls;
    }

    public void setUrls(List<String> urls) {
        this.urls = urls;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }


}
