package com.mlspamdetection.webapp_backend.controller;

import com.mlspamdetection.webapp_backend.dto.AdminRegistrationRequest;
import com.mlspamdetection.webapp_backend.dto.UserDTO;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.service.AdminService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.mlspamdetection.webapp_backend.dto.UserDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    @Autowired
    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        Page<UserDTO> usersPage = adminService.getAllUsers(page, size);
        return ResponseEntity.ok()
            .header("X-Total-Count", String.valueOf(usersPage.getTotalElements()))
            .body(usersPage.getContent());
    }

    @PostMapping("/register-admin")
    public ResponseEntity<?> registerAdmin(
            @RequestHeader("X-Admin-Secret") String adminSecret,
            @Valid @RequestBody AdminRegistrationRequest request) {

        return adminService.registerAdmin(request, adminSecret);
    }

    @PutMapping("/users/{userId}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long userId,
            @RequestBody Map<String, String> roleRequest) {

        String role = roleRequest.get("role");
        return adminService.updateUserRole(userId, role);
    }

    @GetMapping("/stats")
    public ResponseEntity<?> getSystemStats() {
        return ResponseEntity.ok(adminService.getSystemStats());
    }
}
