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

/**
 * Service responsible for administrative operations in the application.
 * 
 * <p>This service provides functionality for managing administrative tasks such as
 * user management, system statistics, and admin user registration. It is designed
 * to be used by users with administrative privileges.</p>
 * 
 * <p>The service includes methods for:</p>
 * <ul>
 *   <li>Retrieving paginated lists of all users</li>
 *   <li>Registering new admin users with proper validation</li>
 *   <li>Updating user roles</li>
 *   <li>Retrieving system-wide statistics</li>
 * </ul>
 * 
 * <p>Security is enforced through a secret key validation for admin registration
 * and appropriate role checks at the controller level.</p>
 */
@Service
public class AdminService {

    /**
     * Repository for accessing user data.
     */
    private final UserRepository userRepository;
    
    /**
     * Repository for accessing prediction log data.
     */
    private final PredictionLogRepository predictionLogRepository;
    
    /**
     * Repository for accessing feedback data.
     */
    private final FeedbackRepository feedbackRepository;
    
    /**
     * Encoder for securely hashing passwords.
     */
    private final PasswordEncoder passwordEncoder;

    /**
     * Secret key required for admin registration, injected from application properties.
     */
    @Value("${admin.secret.key}")
    private String adminSecretKey;

    /**
     * Constructs an AdminService with the necessary dependencies.
     *
     * @param userRepository repository for user data access
     * @param predictionLogRepository repository for prediction log data access
     * @param feedbackRepository repository for feedback data access
     * @param passwordEncoder encoder for securely hashing passwords
     */
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

    /**
     * Retrieves a paginated list of all users in the system.
     * 
     * <p>This method returns users sorted by creation date in descending order (newest first),
     * with pagination support. Each user is converted to a UserDTO to ensure only
     * appropriate information is exposed.</p>
     *
     * @param page the page number to retrieve (zero-based)
     * @param size the number of users per page
     * @return a page of UserDTO objects representing the users
     */
    public Page<UserDTO> getAllUsers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return userRepository.findAll(pageable)
            .map(UserDTO::new);
    }

    /**
     * Registers a new administrator user in the system.
     * 
     * <p>This method creates a new user with administrative privileges. It includes
     * several validation steps:</p>
     * <ol>
     *   <li>Validates that the provided secret key matches the configured admin secret</li>
     *   <li>Checks if the email is already registered in the system</li>
     *   <li>Creates a new user with ADMIN role if validations pass</li>
     * </ol>
     * 
     * <p>Admin users are automatically marked as verified and don't require email verification.</p>
     *
     * @param request the registration request containing email and password
     * @param providedSecret the secret key provided for admin registration
     * @return a ResponseEntity with appropriate status code and message:
     *         <ul>
     *           <li>201 CREATED if admin was successfully registered</li>
     *           <li>403 FORBIDDEN if the provided secret key is invalid</li>
     *           <li>409 CONFLICT if the email is already registered</li>
     *         </ul>
     */
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

    /**
     * Updates a user's role in the system.
     * 
     * <p>This method allows administrators to change a user's role (e.g., from USER to ADMIN
     * or vice versa). It includes validation to ensure:</p>
     * <ol>
     *   <li>The specified user exists in the system</li>
     *   <li>The provided role is a valid UserRole enum value</li>
     * </ol>
     *
     * @param userId the ID of the user whose role should be updated
     * @param role the new role to assign to the user (case-insensitive string matching UserRole enum)
     * @return a ResponseEntity with appropriate status code and message:
     *         <ul>
     *           <li>200 OK if the role was successfully updated</li>
     *           <li>404 NOT_FOUND if the user does not exist</li>
     *           <li>400 BAD_REQUEST if the specified role is invalid</li>
     *         </ul>
     */
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

    /**
     * Retrieves system-wide statistics for administrative dashboards.
     * 
     * <p>This method collects and returns key metrics about the system's usage, including:</p>
     * <ul>
     *   <li>Total number of registered users</li>
     *   <li>Total number of predictions made</li>
     *   <li>Total number of feedback submissions</li>
     * </ul>
     * 
     * <p>These statistics provide administrators with a high-level overview of system activity
     * and usage patterns.</p>
     *
     * @return a SystemStatsDTO containing the collected statistics
     */
    public SystemStatsDTO getSystemStats() {
        SystemStatsDTO stats = new SystemStatsDTO();

        stats.setTotalUsers(userRepository.count());
        stats.setTotalPredictions(predictionLogRepository.count());
        stats.setTotalFeedback(feedbackRepository.count());

        return stats;
    }
}
