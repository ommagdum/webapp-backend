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
import org.springframework.security.core.parameters.P;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class SpamCheckController {

    @Autowired
    private MLServiceClient mlServiceClient;

    @Autowired
    private PredictionLogRepository logRepository;

    @Autowired
    private UserRepository userRepository;

    @PostMapping("/check-spam")
    public ResponseEntity<?> checkSpam(
            @RequestBody SpamCheckRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        PredictionResult result = mlServiceClient.getPrediction(request.getEmailText());

        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));

        PredictionLog log = new PredictionLog();
        log.setUser(user);
        log.setContent(request.getEmailText());
        log.setIs_spam(result.getPrediction().equalsIgnoreCase("spam"));
        log.setConfidence(result.getProbability());
        logRepository.save(log);

        return ResponseEntity.ok(result);
    }


}
