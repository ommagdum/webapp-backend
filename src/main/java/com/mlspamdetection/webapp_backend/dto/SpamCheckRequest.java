package com.mlspamdetection.webapp_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class SpamCheckRequest {

    @NotBlank(message = "Content cannot be empty")
    @Size(max = 10000, message = "Content must not exceed 10000 characters")
    private String content;

    // Getters and setters
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
