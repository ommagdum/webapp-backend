package com.mlspamdetection.webapp_backend.repo;

import com.mlspamdetection.webapp_backend.dto.PredictionHistoryResponse;
import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link PredictionLog} entities.
 * 
 * <p>This repository provides methods to store, retrieve, and analyze spam detection
 * predictions made by the ML model. It extends Spring Data JPA's {@link JpaRepository}
 * to leverage built-in data access functionality.</p>
 * 
 * <p>The repository includes methods for retrieving user-specific prediction history,
 * calculating prediction statistics, and supporting analytics features.</p>
 */
@Repository
public interface PredictionLogRepository extends JpaRepository<PredictionLog, Long> {
    /**
     * Retrieves a paginated list of prediction logs for a specific user, ordered by timestamp (newest first).
     * 
     * <p>This method is primarily used for displaying a user's prediction history in the UI.</p>
     * 
     * @param user the user whose prediction logs to retrieve
     * @param pageable pagination information including page number, page size, and sorting
     * @return a Page of PredictionLog entities
     */
    Page<PredictionLog> findByUserOrderByTimestampDesc(User user, Pageable pageable);

    /**
     * Finds a specific prediction log by its ID and associated user.
     * 
     * <p>This method ensures that users can only access their own prediction logs,
     * enforcing data privacy and security.</p>
     * 
     * @param id the ID of the prediction log to find
     * @param user the user who owns the prediction log
     * @return an Optional containing the prediction log if found, or empty if no matching log exists
     */
    Optional<PredictionLog> findByIdAndUser(Long id, User user);

    /**
     * Counts the total number of prediction logs for a specific user.
     * 
     * <p>This method is used for user statistics and analytics dashboards.</p>
     * 
     * @param user the user whose prediction logs to count
     * @return the total count of prediction logs for the user
     */
    long countByUser(User user);

    /**
     * Counts the number of spam or non-spam prediction logs for a specific user.
     * 
     * <p>This method is used for generating user statistics and analytics dashboards,
     * showing the breakdown of spam vs. non-spam content.</p>
     * 
     * @param user the user whose prediction logs to count
     * @param isSpam true to count spam predictions, false to count non-spam predictions
     * @return the count of spam or non-spam prediction logs for the user
     */
    long countByUserAndIsSpam(User user, boolean isSpam);

    /**
     * Counts the number of prediction logs for a specific user within a date range.
     * 
     * <p>This method supports time-based analytics, allowing the application to show
     * prediction trends over specific time periods.</p>
     * 
     * @param user the user whose prediction logs to count
     * @param start the start of the date range (inclusive)
     * @param end the end of the date range (exclusive)
     * @return the count of prediction logs within the specified date range
     */
    @Query("SELECT COUNT(p) FROM PredictionLog p WHERE p.user = ?1 AND p.timestamp >= ?2 AND p.timestamp < ?3")
    long countByUserAndTimestampBetween(User user, LocalDateTime start, LocalDateTime end);

    /**
     * Retrieves daily prediction counts for a specific user starting from a given date.
     * 
     * <p>This method is used for generating time-series charts and graphs in the analytics
     * dashboard, showing prediction activity over time.</p>
     * 
     * @param user the user whose prediction logs to analyze
     * @param startDate the starting date for the analysis
     * @return a list of date-count pairs, where each pair contains a date and the count of predictions on that date
     */
    @Query("SELECT CAST(p.timestamp AS DATE) as date, COUNT(p) as count FROM PredictionLog p WHERE p.user = ?1 AND p.timestamp >= ?2 GROUP BY CAST(p.timestamp AS DATE) ORDER BY date")
    List<Object[]> getDailyCounts(User user, LocalDateTime startDate);

    /**
     * Retrieves a formatted prediction history for a specific user.
     * 
     * <p>This method uses a projection query to return a simplified view of prediction logs,
     * optimized for display in the UI. It includes only the necessary fields and truncates
     * the content to a preview snippet.</p>
     * 
     * @param user the user whose prediction history to retrieve
     * @param pageable pagination information including page number, page size, and sorting
     * @return a Page of PredictionHistoryItem DTOs containing formatted prediction history
     */
    @Query("SELECT new com.mlspamdetection.webapp_backend.dto.PredictionHistoryResponse$PredictionHistoryItem(" +
            "p.id, SUBSTRING(p.content, 1, 50), p.isSpam, p.confidence, p.timestamp) " +
            "FROM PredictionLog p WHERE p.user = :user")
    Page<PredictionHistoryResponse.PredictionHistoryItem> findHistoryByUser(User user, Pageable pageable);

    /**
     * Finds a specific prediction log by its ID and the ID of its associated user.
     * 
     * <p>This method is similar to {@link #findByIdAndUser(Long, User)} but accepts a user ID
     * instead of a User entity, which can be more convenient in certain scenarios.</p>
     * 
     * @param predictionId the ID of the prediction log to find
     * @param userId the ID of the user who owns the prediction log
     * @return an Optional containing the prediction log if found, or empty if no matching log exists
     */
    Optional<PredictionLog> findByIdAndUserId(Long predictionId, Long userId);
}
