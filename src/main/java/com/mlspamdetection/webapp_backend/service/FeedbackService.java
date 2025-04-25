package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.FeedbackRequest;
import com.mlspamdetection.webapp_backend.model.Feedback;
import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.FeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;
    private final PredictionService predictionService;

    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, PredictionService predictionService){
        this.feedbackRepository = feedbackRepository;
        this.predictionService = predictionService;
    }

    @Transactional
    public boolean submitFeedback(FeedbackRequest request ,User user){

        // Validate prediction belongs to user
        Optional<PredictionLog> predictionOpt =
                Optional.ofNullable(predictionService.getPredictionById(request.getPredictionId(), user));
        if(predictionOpt.isEmpty()){
            return false;
        }

        // Validate Corrected label
        String label = request.getCorrectedLabel();
        if(!"SPAM".equals(label) && !"HAM".equals(label)){
            return false;
        }

        // Check if feedback already exists
        Optional<Feedback> existingFeedback =
                feedbackRepository.findByPredictionIdAndUser(request.getPredictionId(), user);

        Feedback feedback;
        if(existingFeedback.isPresent()) {
            // Update existing feedback
            feedback = existingFeedback.get();
            feedback.setCorrectedLabel(label);
            feedback.setTimestamp(LocalDateTime.now());
            feedback.setProcessed(false);
        } else {
            // Create new feedback
            feedback = new Feedback();
            feedback.setPredictionId(request.getPredictionId());
            feedback.setCorrectedLabel(label);
            feedback.setTimestamp(LocalDateTime.now());
            feedback.setProcessed(false);
            feedback.setUser(user);
        }

        feedbackRepository.save(feedback);
        return true;
    }    }