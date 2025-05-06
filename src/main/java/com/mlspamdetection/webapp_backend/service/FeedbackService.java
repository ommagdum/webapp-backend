package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.FeedbackRequest;
import com.mlspamdetection.webapp_backend.model.Feedback;
import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.FeedbackRepository;
import com.mlspamdetection.webapp_backend.repo.PredictionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PredictionLogRepository predictionLogRepository;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, PredictionLogRepository predictionLogRepository){
        this.feedbackRepository = feedbackRepository;
        this.predictionLogRepository = predictionLogRepository;
    }

    @Transactional
    public void submitFeedback(FeedbackRequest request, User user) {
        // Get prediction WITH user validation
        PredictionLog prediction = predictionLogRepository.findByIdAndUser(request.getPredictionId(), user)
            .orElseThrow(() -> new IllegalArgumentException("Prediction not found"));

        // Validate Corrected label
        String label = request.getCorrectedLabel();
        if(!"spam".equals(label) && !"ham".equals(label)) {
            throw new IllegalArgumentException("Invalid label");
        }

        // Check if feedback exists
        Optional<Feedback> existingFeedback = 
            feedbackRepository.findByPredictionAndUser(prediction, user);

        Feedback feedback;
        if(existingFeedback.isPresent()) {
            feedback = existingFeedback.get();
            feedback.setCorrectedLabel(label);
            feedback.setTimestamp(LocalDateTime.now());
            feedback.setProcessed(false);
        } else {
            feedback = new Feedback();
            feedback.setPrediction(prediction);
            feedback.setCorrectedLabel(label);
            feedback.setUser(user);
            feedback.setTimestamp(LocalDateTime.now());
            feedback.setProcessed(false);
        }

        feedbackRepository.save(feedback);

    }
}