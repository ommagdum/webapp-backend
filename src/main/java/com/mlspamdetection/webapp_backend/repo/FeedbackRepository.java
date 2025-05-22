package com.mlspamdetection.webapp_backend.repo;

import com.mlspamdetection.webapp_backend.model.Feedback;
import com.mlspamdetection.webapp_backend.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.mlspamdetection.webapp_backend.model.PredictionLog;

/**
 * Repository interface for managing {@link Feedback} entities.
 * 
 * <p>This repository provides methods to store, retrieve, and analyze user feedback
 * on spam detection predictions. It extends Spring Data JPA's {@link JpaRepository}
 * to leverage built-in data access functionality.</p>
 * 
 * <p>The repository includes methods for retrieving user feedback, calculating feedback
 * statistics, and identifying predictions that need correction. This feedback data is
 * essential for the continuous improvement of the ML model through retraining.</p>
 */
@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    /**
     * Finds feedback for a specific prediction from a specific user.
     * 
     * <p>This method is used to check if a user has already provided feedback for a
     * particular prediction, preventing duplicate feedback entries.</p>
     * 
     * @param prediction the prediction log for which to find feedback
     * @param user the user who provided the feedback
     * @return an Optional containing the feedback if found, or empty if no matching feedback exists
     */
    Optional<Feedback> findByPredictionAndUser(PredictionLog prediction, User user);

    /**
     * Counts the number of processed or unprocessed feedback entries for a specific user.
     * 
     * <p>This method is used for generating user statistics and tracking how much of a
     * user's feedback has been incorporated into model retraining.</p>
     * 
     * @param user the user whose feedback to count
     * @param processed true to count processed feedback, false to count unprocessed feedback
     * @return the count of processed or unprocessed feedback entries for the user
     */
    long countByUserAndProcessed(User user, boolean processed);

    /**
     * Counts the number of predictions where the model was correct according to user feedback.
     * 
     * <p>This method is used to evaluate model performance by counting cases where
     * the user's feedback confirms the original prediction (i.e., the model was correct).
     * It helps track model accuracy from the user's perspective.</p>
     * 
     * @param user the user whose feedback to analyze
     * @return the count of predictions that were confirmed correct by user feedback
     */
    @Query("""
    SELECT COUNT(f) 
    FROM Feedback f 
    JOIN f.prediction p 
    WHERE f.user = ?1 
      AND f.processed = true 
      AND ((p.isSpam = true AND f.correctedLabel = 'SPAM') 
           OR (p.isSpam = false AND f.correctedLabel = 'HAM'))
    """)
    long countCorrectPredictions(User user);

    /**
     * Retrieves a list of feedback entries based on their processing status.
     * 
     * <p>This method is primarily used by the model retraining process to find feedback
     * that has not yet been incorporated into the model. Once feedback is used for
     * retraining, it is marked as processed.</p>
     * 
     * @param processed true to find processed feedback, false to find unprocessed feedback
     * @return a list of feedback entries with the specified processing status
     */
    List<Feedback> findByProcessed(boolean processed);
}
