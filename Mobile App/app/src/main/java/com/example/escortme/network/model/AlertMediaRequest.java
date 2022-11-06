package com.example.escortme.network.model;

import java.util.List;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;

public class AlertMediaRequest {
    private String tag;
    private String body;

    List<MultipartBody.Part> files;

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public List<MultipartBody.Part> getFiles() {
        return files;
    }

    public void setFiles(List<MultipartBody.Part> files) {
        this.files = files;
    }
}
