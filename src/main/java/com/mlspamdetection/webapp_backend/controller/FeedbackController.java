package com.mlspamdetection.webapp_backend.controller;

import com.mlspamdetection.webapp_backend.dto.FeedbackRequest;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.service.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/feedback")
public class FeedbackController {

    private final FeedbackService feedbackService;

    @Autowired
    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping
    public ResponseEntity<Void> submitFeedback(
            @RequestBody FeedbackRequest request,
            @AuthenticationPrincipal User user) {

        boolean success = feedbackService.submitFeedback(request, user);

        if (success) {
            return new ResponseEntity<>(HttpStatus.CREATED);
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
