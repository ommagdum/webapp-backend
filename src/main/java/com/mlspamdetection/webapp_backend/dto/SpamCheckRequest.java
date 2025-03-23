package com.mlspamdetection.webapp_backend.dto;

public class SpamCheckRequest {
    private String emailText;

    public SpamCheckRequest() {
    }

    public SpamCheckRequest(String emailText) {
        this.emailText = emailText;
    }

    public String getEmailText() {
        return emailText;
    }

    public void setEmailText(String emailText) {
        this.emailText = emailText;
    }
}
