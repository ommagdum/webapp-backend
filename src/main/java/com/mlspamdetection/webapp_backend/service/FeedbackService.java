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

/**
 * Service responsible for managing user feedback on spam predictions.
 * 
 * <p>This service provides functionality for users to submit feedback on spam detection
 * predictions, indicating whether they agree with the classification or providing a
 * corrected label. This feedback is essential for improving the ML model through retraining.</p>
 * 
 * <p>The service ensures that feedback is properly validated, stored, and marked for
 * processing during the next model retraining cycle. It also handles updating existing
 * feedback if a user changes their assessment of a prediction.</p>
 */
@Service
public class FeedbackService {

    /**
     * Repository for accessing feedback data.
     */
    private final FeedbackRepository feedbackRepository;
    
    /**
     * Repository for accessing prediction log data.
     */
    private final PredictionLogRepository predictionLogRepository;

    /**
     * Constructs a FeedbackService with the necessary dependencies.
     *
     * @param feedbackRepository repository for feedback data access
     * @param predictionLogRepository repository for prediction log data access
     */
    @Autowired
    public FeedbackService(FeedbackRepository feedbackRepository, PredictionLogRepository predictionLogRepository){
        this.feedbackRepository = feedbackRepository;
        this.predictionLogRepository = predictionLogRepository;
    }

    /**
     * Submits user feedback for a specific prediction.
     * 
     * <p>This method allows users to provide feedback on a prediction by specifying
     * the correct classification (spam or ham). The feedback is used to improve the
     * ML model through retraining.</p>
     * 
     * <p>The method follows these steps:</p>
     * <ol>
     *   <li>Validates that the prediction exists and belongs to the user</li>
     *   <li>Validates that the corrected label is either "spam" or "ham"</li>
     *   <li>Checks if the user has already provided feedback for this prediction</li>
     *   <li>Updates existing feedback or creates new feedback as appropriate</li>
     *   <li>Marks the feedback as unprocessed so it will be included in the next retraining cycle</li>
     * </ol>
     *
     * <p>The method is transactional to ensure database consistency when creating or
     * updating feedback records.</p>
     *
     * @param request the feedback request containing the prediction ID and corrected label
     * @param user the user submitting the feedback
     * @throws IllegalArgumentException if the prediction is not found or the label is invalid
     */
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