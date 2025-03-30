package com.mlspamdetection.webapp_backend.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.mlspamdetection.webapp_backend.dto.PredictionResponse;
import com.mlspamdetection.webapp_backend.dto.PredictionResult;
import com.mlspamdetection.webapp_backend.dto.SpamCheckRequest;
import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.PredictionLogRepository;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import com.mlspamdetection.webapp_backend.service.MLServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpamController {

    @Autowired
    private MLServiceClient mlServiceClient;

    @Autowired
    private PredictionLogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/predict")
    public ResponseEntity<?> checkSpam(@RequestBody(required = false) Map<String, Object> request) {
        try {
            System.out.println("Received request: " + request);

            // Check if request is null or empty
            if (request == null || request.isEmpty()) {
                System.out.println("Request is null or empty");
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "Request body is required"));
            }

            // Get the authenticated user
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String username = null;
            if (authentication != null && authentication.isAuthenticated() &&
                    !(authentication.getPrincipal() instanceof String &&
                            authentication.getPrincipal().equals("anonymousUser"))) {
                username = authentication.getName();
                System.out.println("Authenticated user: " + username);
            } else {
                System.out.println("Authentication failed: no valid authentication found");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Collections.singletonMap("error", "Authentication required"));
            }

            // Get and validate content
            Object contentObj = request.get("content");
            if (contentObj == null) {
                System.out.println("Content validation failed: content is null");
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "Content is required"));
            }

            String emailText;
            if (contentObj instanceof String) {
                emailText = (String) contentObj;
            } else {
                System.out.println("Content validation failed: content is not a string: " + contentObj.getClass().getName());
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "Content must be a string"));
            }

            System.out.println("Email content: " + (emailText.length() > 50 ? emailText.substring(0, 50) + "..." : emailText));

            if (emailText.isEmpty()) {
                System.out.println("Content validation failed: content is empty");
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "Content is required"));
            }

            if (emailText.length() > 10000) {
                System.out.println("Content validation failed: content exceeds 10,000 characters");
                return ResponseEntity.badRequest()
                        .body(Collections.singletonMap("error", "Content exceeds 10,000 characters"));
            }

            // Get prediction
            System.out.println("Calling ML service for prediction");
            PredictionResult mlResult = mlServiceClient.getPrediction(emailText);
            System.out.println("ML service response: " + mlResult);

            if (mlResult == null || mlResult.getPrediction() == null) {
                System.out.println("ML service error: result is null or prediction is null");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Collections.singletonMap("error", "Prediction service error"));
            }

            // Save log
            System.out.println("Looking up user: " + username);
            User user = userRepository.findByEmail(username)
                    .orElseThrow(() -> new RuntimeException("User not found"));
            System.out.println("User found: " + user.getId());

            PredictionLog log = new PredictionLog();
            log.setUser(user);
            log.setContent(emailText);
            log.setIs_spam(mlResult.getPrediction().equalsIgnoreCase("spam"));
            log.setConfidence(mlResult.getProbability());
            logRepository.save(log);
            System.out.println("Prediction log saved");

            // Return standardized response
            PredictionResponse response = new PredictionResponse(
                    mlResult.getPrediction().equalsIgnoreCase("spam") ? 1 : 0,
                    mlResult.getProbability()
            );
            System.out.println("Returning response: " + response);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.out.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError()
                    .body(Collections.singletonMap("error", "Failed to process prediction: " + e.getMessage()));
        }
    }

}
