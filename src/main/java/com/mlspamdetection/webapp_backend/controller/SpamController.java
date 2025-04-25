package com.mlspamdetection.webapp_backend.controller;

import com.mlspamdetection.webapp_backend.dto.PredictionResponse;
import com.mlspamdetection.webapp_backend.dto.PredictionResult;
import com.mlspamdetection.webapp_backend.dto.SpamCheckRequest;
import com.mlspamdetection.webapp_backend.exception.UserNotFoundException;import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.PredictionLogRepository;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import com.mlspamdetection.webapp_backend.service.MLServiceClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SpamController {

    private static final Logger logger = LoggerFactory.getLogger(SpamController.class);

    private final MLServiceClient mlServiceClient;
    private final PredictionLogRepository logRepository;
    private final UserRepository userRepository;

    public SpamController(MLServiceClient mlServiceClient, PredictionLogRepository logRepository, UserRepository userRepository) {
        this.mlServiceClient = mlServiceClient;
        this.logRepository = logRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/predict")
    public ResponseEntity<PredictionResponse> checkSpam(@AuthenticationPrincipal User user, @Valid @RequestBody SpamCheckRequest request) {
        try {
            String emailText = request.getContent();

            if (emailText == null || emailText.isEmpty()) {
                logger.warn("Content is required");
                return ResponseEntity.badRequest().body(new PredictionResponse(-1, 0, "Content is required"));
            }

            if (emailText.length() > 10000) {
                logger.warn("Content exceeds 10,000 characters");
                return ResponseEntity.badRequest().body(new PredictionResponse(-1, 0, "Content exceeds 10,000 characters"));
            }

            // Add email validation here

            PredictionResult mlResult = mlServiceClient.getPrediction(emailText);

            if (mlResult == null || mlResult.getPrediction() == null) {
                logger.error("Prediction service error: null result");
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new PredictionResponse(-1, 0, "Prediction service error"));
            }

            if (user == null) {
                logger.error("User not found after successful authentication");
                throw new UserNotFoundException("User not found");
            }

            PredictionLog log = new PredictionLog();
            log.setUser(user);
            log.setContent(emailText);
            log.setSpam(mlResult.getPrediction().equalsIgnoreCase("spam"));
            log.setConfidence(mlResult.getProbability());
            logRepository.save(log);

            int prediction = mlResult.getPrediction().equalsIgnoreCase("spam") ? 1 : 0;
            double probability = mlResult.getProbability();

            logger.info("Prediction for user {}: prediction={}, probability={}", user.getEmail(), prediction, probability);

            return ResponseEntity.ok(new PredictionResponse(prediction, probability, null));

        } catch (UserNotFoundException e) {
            logger.error("User not found: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new PredictionResponse(-1, 0, "User not found"));
        } catch (Exception e) {
            logger.error("Failed to process prediction: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().body(new PredictionResponse(-1, 0, "Failed to process prediction"));
        }
    }
}
