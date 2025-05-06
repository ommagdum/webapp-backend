package com.mlspamdetection.webapp_backend.repo;

import com.mlspamdetection.webapp_backend.model.Feedback;
import com.mlspamdetection.webapp_backend.model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

import com.mlspamdetection.webapp_backend.model.PredictionLog;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    Optional<Feedback> findByPredictionAndUser(PredictionLog prediction, User user);

    long countByUserAndProcessed(User user, boolean processed);

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

    List<Feedback> findByProcessed(boolean processed);
}
