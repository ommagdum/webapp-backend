package com.mlspamdetection.webapp_backend.controller;

import com.mlspamdetection.webapp_backend.dto.*;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import com.mlspamdetection.webapp_backend.security.JwtUtil;
import com.mlspamdetection.webapp_backend.service.EmailVerificationService;
import com.mlspamdetection.webapp_backend.service.UserService;
import com.mlspamdetection.webapp_backend.util.GoogleTokenVerifier;
import com.mlspamdetection.webapp_backend.util.GoogleUserData;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailVerificationService emailVerificationService;

    @Autowired
    private GoogleTokenVerifier googleTokenVerifier;

    @Autowired
    private UserService userService;

    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
                          PasswordEncoder passwordEncoder, JwtUtil jwtUtil, EmailVerificationService emailVerificationService) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailVerificationService = emailVerificationService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest registerRequest){
        if (userRepository.existsByEmail(registerRequest.getEmail())){
            return ResponseEntity
                    .badRequest()
                    .body("Email is already registered");
        }

        User user = new User();
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setVerified(false);
        String verificationCode = UUID.randomUUID().toString();
        user.setVerificationToken(verificationCode);

        userRepository.save(user);

        try {
            emailVerificationService.sendVerificationEmail(user);
            return ResponseEntity.ok("Registration successful! Please check your email to verify your account.");
        } catch (MessagingException e) {
            return ResponseEntity
                    .status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send verification email: " + e.getMessage());
        }

    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(auth -> auth.replaceFirst("^ROLE_", ""))
                    .collect(Collectors.toList());
            String jwt = jwtUtil.generateToken(userDetails, roles);
            return ResponseEntity.ok().body(Map.of(
                    "token", jwt,
                    "message", "Login successful"
            ));

        } catch (BadCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Invalid credentials"));
        }
    }
    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        Optional<User> userOptional = userRepository.findByVerificationToken(token);

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            user.setVerified(true);
            user.setVerificationToken(null); // Clear the token after verification
            userRepository.save(user);

            return ResponseEntity.ok("Email verified successfully. You can now log in.");
        } else {
            return ResponseEntity.badRequest().body("Invalid verification token");
        }
    }


    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody ResendVerificationRequest request) {
        try {
            boolean sent = emailVerificationService.resendVerificationEmail(request.getEmail());

            if (sent) {
                return ResponseEntity.ok(Collections.singletonMap("message",
                        "Verification email has been resent. Please check your inbox."));
            } else {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error",
                        "User not found or already verified."));
            }
        } catch (MessagingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Collections.singletonMap("error", "Failed to send verification email"));
        }
    }

    @PostMapping("/google-auth")
    public ResponseEntity<?> googleAuth(@RequestBody Map<String, String> request) {
        try {
            String googleToken = request.get("credential");
            System.out.println("Received Google token: " + googleToken.substring(0, 20) + "...");

            // Verify token with Google API
            GoogleUserData userData = googleTokenVerifier.verify(googleToken);
            System.out.println("Verified Google user: " + userData.getEmail());

            // Create/login user
            User user = userService.findOrCreateGoogleUser(userData);
            System.out.println("User found/created: " + user.getEmail());

            // Generate JWT
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(auth -> auth.replaceFirst("^ROLE_", ""))
                    .collect(Collectors.toList());
            String jwt = jwtUtil.generateToken(userDetails, roles);
            System.out.println("JWT generated successfully");

            return ResponseEntity.ok()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + jwt)
                    .body(Collections.singletonMap("token", jwt));

        } catch (Exception e) {
            System.err.println("Google authentication failed: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    Collections.singletonMap("error", "Google authentication failed: " + e.getMessage())
            );
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader(value = "Authorization", required = false) String authHeader) {
        System.out.println("Auth header: " + authHeader);

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authentication token provided");
        }

        String token = authHeader.substring(7);
        String username = jwtUtil.extractUsername(token);

        if (username != null) {
            Optional<User> userOpt = userRepository.findByEmail(username);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                Map<String, Object> response = new HashMap<>();
                response.put("email", user.getEmail());
                response.put("id", user.getId());
                // Add other user details as needed, but exclude sensitive info like password

                return ResponseEntity.ok(response);
            }
        }

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid authentication token");
    }
    @PostMapping("/refresh-token")
    public ResponseEntity<?> refreshToken(@RequestBody TokenRefreshRequest request) {
        try {
            // Validate refresh token
            String refreshToken = request.getRefreshToken();
            if (refreshToken == null || refreshToken.isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Collections.singletonMap("error", "Refresh token is required"));
            }

            // Extract username from token
            String username = jwtUtil.extractUsername(refreshToken);
            if (username == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Invalid refresh token"));
            }

            // Find user by email
            Optional<User> userOpt = userRepository.findByEmail(username);
            if (userOpt.isEmpty() || !refreshToken.equals(userOpt.get().getRefreshToken())) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Invalid refresh token"));
            }

            User user = userOpt.get();

            // Generate new tokens
            UserDetails userDetails = org.springframework.security.core.userdetails.User.builder()
                    .username(user.getEmail())
                    .password(user.getPassword())
                    .roles("USER")
                    .build();

            List<String> roles = userDetails.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(auth -> auth.replaceFirst("^ROLE_", ""))
                    .collect(Collectors.toList());
            String newAccessToken = jwtUtil.generateToken(userDetails, roles);
            String newRefreshToken = UUID.randomUUID().toString(); // Or use jwtUtil to generate a refresh token

            // Update refresh token in database
            user.setRefreshToken(newRefreshToken);
            userRepository.save(user);

            // Return new tokens
            Map<String, String> response = new HashMap<>();
            response.put("accessToken", newAccessToken);
            response.put("refreshToken", newRefreshToken);

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Failed to refresh token: " + e.getMessage()));
        }
    }


}