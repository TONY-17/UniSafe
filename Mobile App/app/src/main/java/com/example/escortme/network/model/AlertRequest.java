package com.example.escortme.network.model;

public class AlertRequest {
    private String tag;
    private String subject;
    private String body;
    private String timePosted;
    private boolean reported;

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public void setTimePosted(String timePosted) {
        this.timePosted = timePosted;
    }

    public void setReported(boolean reported) {
        this.reported = reported;
    }
}
