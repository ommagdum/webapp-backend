package com.mlspamdetection.webapp_backend.repo;

import com.mlspamdetection.webapp_backend.model.RetrainingReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for managing {@link RetrainingReport} entities.
 * 
 * <p>This repository provides methods to store, retrieve, and manage retraining reports
 * for the ML spam detection model. It extends Spring Data JPA's {@link JpaRepository}
 * to leverage built-in data access functionality.</p>
 * 
 * <p>The repository includes methods for retrieving retraining reports based on various
 * criteria such as timestamp, active status, and model version. It also provides functionality
 * to manage the active model in the system.</p>
 * 
 * <p>Retraining reports track the history of model retraining operations, including performance
 * metrics, success status, and which model version is currently active in the system.</p>
 */
@Repository
public interface RetrainingReportRepository extends JpaRepository<RetrainingReport, Long> {

    /**
     * Finds retraining reports within a specified time range, ordered by timestamp in descending order.
     * 
     * <p>This method is used to retrieve the history of model retraining operations
     * that occurred within a specific time period, with the most recent reports first.</p>
     * 
     * @param start the start of the time range (inclusive)
     * @param end the end of the time range (inclusive)
     * @return a list of retraining reports within the specified time range, ordered by timestamp (newest first)
     */
    List<RetrainingReport> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);

    /**
     * Finds the most recent retraining report based on timestamp.
     * 
     * <p>This method is used to retrieve the latest retraining report, regardless of
     * its success status or active state. It helps track the most recent attempt to
     * retrain the model.</p>
     * 
     * @return the most recent retraining report
     */
    RetrainingReport findTopByOrderByTimestampDesc();

    /**
     * Finds the currently active retraining report.
     * 
     * <p>This method is used to retrieve the retraining report associated with the
     * model version that is currently active in the system. There should be only
     * one active model at any given time.</p>
     * 
     * @return an Optional containing the active retraining report, or empty if no active report exists
     */
    Optional<RetrainingReport> findByIsActiveTrue();

    /**
     * Finds the most recent successful retraining report.
     * 
     * <p>This method is used to retrieve the latest retraining report that was successful,
     * which can be used as a fallback when a new model training fails or to compare
     * performance metrics between the current and previous successful models.</p>
     * 
     * @return an Optional containing the most recent successful retraining report, or empty if no successful report exists
     */
    Optional<RetrainingReport> findTopBySuccessTrueOrderByTimestampDesc();

    /**
     * Deactivates all model versions in the system.
     * 
     * <p>This method sets the isActive flag to false for all retraining reports,
     * effectively marking all models as inactive. This is typically used before
     * activating a new model to ensure that only one model is active at a time.</p>
     * 
     * <p>The method is annotated with {@link Modifying} and {@link Transactional}
     * to ensure that the update operation is performed within a transaction.</p>
     */
    @Modifying
    @Transactional
    @Query("UPDATE RetrainingReport r SET r.isActive = false")
    void deactivateAllModels();

    /**
     * Finds a retraining report by its model version identifier.
     * 
     * <p>This method is used to retrieve a specific retraining report based on the
     * unique model version identifier. This is useful for tracking specific model
     * versions or retrieving details about a particular model.</p>
     * 
     * @param modelVersion the unique identifier for the model version
     * @return an Optional containing the retraining report for the specified model version, or empty if not found
     */
    Optional<RetrainingReport> findByModelVersion(String modelVersion);
}
