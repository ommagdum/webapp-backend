package com.mlspamdetection.webapp_backend.controller;

import com.mlspamdetection.webapp_backend.dto.PredictionResult;
import com.mlspamdetection.webapp_backend.dto.SpamCheckRequest;
import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.PredictionLogRepository;
import com.mlspamdetection.webapp_backend.repo.UserRepository;
import com.mlspamdetection.webapp_backend.service.MLServiceClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity<?> checkSpam(
            @RequestBody Map<String, String> request,
            @AuthenticationPrincipal UserDetails userDetails) {

        String emailText = request.get("email_text");
        if (emailText == null) {
            return ResponseEntity.badRequest().body("email_text is required");
        }

        PredictionResult mlResult = mlServiceClient.getPrediction(emailText);

        User user = userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PredictionLog log = new PredictionLog();
        log.setUser(user);
        log.setContent(emailText);
        log.setIs_spam(mlResult.getPrediction().equalsIgnoreCase("spam"));
        log.setConfidence(mlResult.getProbability());
        logRepository.save(log);

        Map<String, Object> response = new HashMap<>();
        response.put("prediction", mlResult.getPrediction().equalsIgnoreCase("spam") ? 1 : 0);
        response.put("probability", mlResult.getProbability());
        return ResponseEntity.ok(response);
    }
}
