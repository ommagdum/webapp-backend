package com.mlspamdetection.webapp_backend.controller;

import com.mlspamdetection.webapp_backend.dto.FeedbackRequest;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.service.FeedbackService;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/correct-prediction")
    public ResponseEntity<?> submitPredictionCorrection(
            @Valid @RequestBody FeedbackRequest feedbackRequest,
            @AuthenticationPrincipal User user) {

        try {
            feedbackService.submitFeedback(feedbackRequest, user);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Collections.singletonMap("message", "Feedback received"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }
}
