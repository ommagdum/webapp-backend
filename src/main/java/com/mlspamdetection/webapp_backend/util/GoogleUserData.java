package com.mlspamdetection.webapp_backend.util;

public class GoogleUserData {
    private String googleId;
    private String email;
    private String name;
    private String pictureUrl;

    public GoogleUserData(String subject, String email, String name, String picture) {
    }

    public GoogleUserData() {

    }


    // Getters and setters
    public String getGoogleId() {
        return googleId;
    }

    public void setGoogleId(String googleId) {
        this.googleId = googleId;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public void setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
    }
}
