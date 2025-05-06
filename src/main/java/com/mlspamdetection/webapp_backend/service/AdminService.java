package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.AdminRegistrationRequest;
import com.mlspamdetection.webapp_backend.dto.SystemStatsDTO;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.FeedbackRepository;
import com.mlspamdetection.webapp_backend.repo.PredictionLogRepository;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.mlspamdetection.webapp_backend.dto.UserDTO;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    private final UserRepository userRepository;
    private final PredictionLogRepository predictionLogRepository;
    private final FeedbackRepository feedbackRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.secret.key}")
    private String adminSecretKey;

    @Autowired
    public AdminService(
            UserRepository userRepository,
            PredictionLogRepository predictionLogRepository,
            FeedbackRepository feedbackRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.predictionLogRepository = predictionLogRepository;
        this.feedbackRepository = feedbackRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Page<UserDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable)
            .map(UserDTO::new);
    }

    public ResponseEntity<?> registerAdmin(AdminRegistrationRequest request, String providedSecret) {
        // Validate admin secret
        if (!adminSecretKey.equals(providedSecret)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message", "Invalid admin secret key"));
        }

        // Check if email already exists
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(Map.of("message", "Email already exists"));
        }

        // Create new admin user
        User adminUser = new User();
        adminUser.setEmail(request.getEmail());
        adminUser.setPassword(passwordEncoder.encode(request.getPassword()));
        adminUser.setRole(User.UserRole.ADMIN);
        adminUser.setVerified(true); // Admin is automatically verified

        userRepository.save(adminUser);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Admin user created successfully"));
    }

    public ResponseEntity<?> updateUserRole(Long userId, String role) {
        Optional<User> userOpt = userRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User not found"));
        }

        User user = userOpt.get();

        try {
            User.UserRole newRole = User.UserRole.valueOf(role.toUpperCase());
            user.setRole(newRole);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("message", "User role updated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("message", "Invalid role specified"));
        }
    }

    public SystemStatsDTO getSystemStats() {
        SystemStatsDTO stats = new SystemStatsDTO();

        stats.setTotalUsers(userRepository.count());
        stats.setTotalPredictions(predictionLogRepository.count());
        stats.setTotalFeedback(feedbackRepository.count());

        // You can add more statistics as needed

        return stats;
    }
}
