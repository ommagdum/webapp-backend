package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.PredictionHistoryResponse;
import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.PredictionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service responsible for managing prediction logs and history.
 * 
 * <p>This service provides functionality for retrieving and managing spam detection
 * prediction logs. It works with the {@link PredictionLogRepository} to access
 * prediction data and provides methods for retrieving user-specific prediction history.</p>
 * 
 * <p>The service supports paginated access to prediction history, allowing users to
 * browse through their past predictions efficiently. It also provides methods for
 * retrieving specific prediction logs by ID.</p>
 */
@Service
public class PredictionService {

    /**
     * Repository for accessing prediction log data.
     */
    private final PredictionLogRepository predictionLogRepository;

    /**
     * Constructs a PredictionService with the necessary dependencies.
     *
     * @param predictionLogRepository repository for prediction log data access
     */
    @Autowired
    public PredictionService(PredictionLogRepository predictionLogRepository) {
        this.predictionLogRepository = predictionLogRepository;
    }

    /**
     * Retrieves paginated prediction history for a specific user.
     * 
     * <p>This method returns a paginated list of prediction history items for the specified user,
     * ordered by timestamp in descending order (newest first). The history includes the prediction
     * content, classification result, and timestamp.</p>
     *
     * @param user the user whose prediction history to retrieve
     * @param page the zero-based page index
     * @param size the size of the page to be returned
     * @return a response containing the paginated prediction history and pagination metadata
     */
    public PredictionHistoryResponse getPredictionHistory(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));

        Page<PredictionHistoryResponse.PredictionHistoryItem> predictionPage =
                predictionLogRepository.findHistoryByUser(user, pageable);

        return new PredictionHistoryResponse(
                predictionPage.getContent(),
                predictionPage.getNumber(),
                predictionPage.getTotalPages()
        );
    }

    /**
     * Retrieves a specific prediction log by its ID and the associated user ID.
     * 
     * <p>This method ensures that users can only access their own prediction logs
     * by requiring both the prediction ID and the user ID to match.</p>
     *
     * @param predictionId the ID of the prediction log to retrieve
     * @param userId the ID of the user who owns the prediction log
     * @return an Optional containing the prediction log if found, or empty if not found
     */
    public Optional<PredictionLog> getPredictionLogByPredictionIdAndUserId(
        Long predictionId, Long userId) {
        return predictionLogRepository.findByIdAndUserId(predictionId, userId);
    }
    /**
     * Retrieves a specific prediction log by its ID and the associated user.
     * 
     * <p>This method ensures that users can only access their own prediction logs
     * by requiring both the prediction ID and the user object to match. Unlike the
     * {@link #getPredictionLogByPredictionIdAndUserId} method, this method throws
     * an exception if the prediction log is not found.</p>
     *
     * @param id the ID of the prediction log to retrieve
     * @param user the user who owns the prediction log
     * @return the prediction log if found
     * @throws RuntimeException if the prediction log is not found
     */
    public PredictionLog getPredictionById(Long id, User user) {
        return predictionLogRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new RuntimeException("Prediction not found with id: " + id));
    }

}