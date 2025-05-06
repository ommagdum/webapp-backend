package com.mlspamdetection.webapp_backend.dto;

import com.mlspamdetection.webapp_backend.model.User;
import java.time.LocalDateTime;

public record UserDTO(
    Long id,
    String email,
    User.UserRole role,  // Reference to inner enum
    LocalDateTime createdAt
) {
    public UserDTO(User user) {
        this(user.getId(), user.getEmail(), user.getRole(), user.getCreatedAt());
    }
}