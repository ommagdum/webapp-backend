package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.RetrainingRequest;
import com.mlspamdetection.webapp_backend.dto.RetrainingResponse;
import com.mlspamdetection.webapp_backend.model.Feedback;
import com.mlspamdetection.webapp_backend.model.PredictionLog;
import com.mlspamdetection.webapp_backend.model.RetrainingReport;
import com.mlspamdetection.webapp_backend.repo.FeedbackRepository;
import com.mlspamdetection.webapp_backend.repo.PredictionLogRepository;
import com.mlspamdetection.webapp_backend.repo.RetrainingReportRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Service responsible for managing the ML model retraining process.
 * 
 * <p>This service coordinates the collection of user feedback, preparation of training data,
 * communication with the ML service for retraining, and tracking of model versions and performance
 * metrics. It plays a critical role in the continuous improvement of the spam detection model.</p>
 * 
 * <p>The retraining process involves:</p>
 * <ol>
 *   <li>Collecting unprocessed user feedback on previous predictions</li>
 *   <li>Transforming feedback into a format suitable for model training</li>
 *   <li>Sending the training data to the ML service</li>
 *   <li>Recording the results and updating model version information</li>
 *   <li>Marking feedback as processed to avoid duplicate training</li>
 * </ol>
 * 
 * <p>This service helps maintain an up-to-date ML model that adapts to evolving spam patterns
 * based on user corrections.</p>
 */
@Service
public class RetrainingService {

    /**
     * Logger for this class.
     */
    private static final Logger logger = LoggerFactory.getLogger(RetrainingService.class);

    /**
     * Repository for accessing feedback data.
     */
    private final FeedbackRepository feedbackRepository;
    
    /**
     * Repository for accessing prediction log data.
     */
    private final PredictionLogRepository predictionLogRepository;
    
    /**
     * Repository for accessing and updating retraining report data.
     */
    private final RetrainingReportRepository retrainingReportRepository;
    
    /**
     * RestTemplate for making HTTP requests to the ML service.
     */
    private final RestTemplate restTemplate;

    /**
     * URL of the ML service endpoint, injected from application properties.
     */
    @Value("${ml.service.url}")
    private String mlServiceUrl;

    /**
     * Constructs a RetrainingService with the necessary dependencies.
     *
     * @param feedbackRepository repository for feedback data access
     * @param predictionLogRepository repository for prediction log data access
     * @param retrainingReportRepository repository for retraining report data access
     * @param restTemplate REST client for communicating with the ML service
     */
    @Autowired
    public RetrainingService(
            FeedbackRepository feedbackRepository,
            PredictionLogRepository predictionLogRepository,
            RetrainingReportRepository retrainingReportRepository,
            RestTemplate restTemplate) {
        this.feedbackRepository = feedbackRepository;
        this.predictionLogRepository = predictionLogRepository;
        this.retrainingReportRepository = retrainingReportRepository;
        this.restTemplate = restTemplate;
    }

    /**
     * Processes all unprocessed user feedback to retrain the ML model.
     * 
     * <p>This method performs the complete retraining workflow:</p>
     * <ol>
     *   <li>Collects all feedback marked as unprocessed</li>
     *   <li>Transforms feedback into training data format</li>
     *   <li>Sends the training data to the ML service</li>
     *   <li>Updates feedback status to processed</li>
     *   <li>Records retraining results and model performance metrics</li>
     *   <li>Updates the active model version</li>
     * </ol>
     * 
     * <p>The method is transactional to ensure that all database operations (marking feedback
     * as processed, updating model status) are performed atomically.</p>
     *
     * @return a RetrainingReport containing information about the retraining process, including
     *         success status, model metrics, and version information
     */
    @Transactional
    public RetrainingReport processUnprocessedFeedback() {
        // Collect all unprocessed feedback
        List<Feedback> unprocessedFeedback = feedbackRepository.findByProcessed(false);

        if(unprocessedFeedback.isEmpty()) {
            logger.info("No unprocessed feedback found. Skipping retraining.");
            return createReport(0, "No feedback found for retraining.", false, null, null);
        }

        logger.info("Found {} unprocessed feedback items.", unprocessedFeedback.size());

        // Transform to ml training format
        List<RetrainingRequest.TrainingItem> trainingItems = new ArrayList<>();
        for(Feedback feedback: unprocessedFeedback) {
            Optional<PredictionLog> predictionOpt = predictionLogRepository.findById(feedback.getPrediction().getId());
            if(predictionOpt.isPresent()) {
                PredictionLog prediction = predictionOpt.get();

                RetrainingRequest.TrainingItem item = new RetrainingRequest.TrainingItem();
                item.setContent(prediction.getContent());
                item.setLabel(feedback.getCorrectedLabel());
                trainingItems.add(item);
            } else {
                logger.warn("Could not find prediction log for feedback with ID: {}", feedback.getId());
            }
        }

        if(trainingItems.isEmpty()) {
            logger.warn("No valid training items found. Skipping retraining.");
            return createReport(0, "No valid training items found.", false, null, null);
        }

        // Call ml service
        RetrainingRequest request = new RetrainingRequest();
        request.setTrainingData(trainingItems);

        try {
            ResponseEntity<RetrainingResponse> response = restTemplate.postForEntity(
                    mlServiceUrl + "/retrain", request, RetrainingResponse.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                RetrainingResponse result = response.getBody();

                // Update feedback as processed
                for(Feedback feedback : unprocessedFeedback) {
                    feedback.setProcessed(true);
                }
                feedbackRepository.saveAll(unprocessedFeedback);

                Optional<RetrainingReport> currentActiveReport = retrainingReportRepository.findByIsActiveTrue();
                String previousVersion = currentActiveReport.map(RetrainingReport::getModelVersion).orElse(null);

                // Deactivate all existing models
                retrainingReportRepository.deactivateAllModels();

                // Store report with model version information and set as active
                RetrainingReport report = createReport(
                        trainingItems.size(),
                        "Retraining Successful. " + result.getMessage(),
                        true,
                        result,
                        previousVersion
                );

                return report;
            } else {
                logger.error("Retraining failed. Response: {}", response.getStatusCode());
                return createReport(0, "Retraining failed. Response: " + response.getStatusCode(), false, null, null);
            }
        } catch (Exception e) {
            logger.error("Error during retraining: ", e);
            return createReport(0, "Error during retraining: " + e.getMessage(), false, null, null);
        }
    }

    /**
     * Creates and persists a retraining report with the given parameters.
     * 
     * <p>This helper method creates a new RetrainingReport entity with information about
     * a retraining attempt, including:</p>
     * <ul>
     *   <li>Basic information: timestamp, number of items processed, success status</li>
     *   <li>Previous model version for tracking model lineage</li>
     *   <li>Model performance metrics if retraining was successful</li>
     * </ul>
     * 
     * <p>If the retraining was successful (result is not null), the report will include
     * model metrics such as accuracy, precision, and recall, and the model will be marked
     * as active.</p>
     *
     * @param itemCount number of feedback items processed during retraining
     * @param message descriptive message about the retraining process outcome
     * @param success whether the retraining was successful
     * @param result response from the ML service containing model metrics (may be null if retraining failed)
     * @param previousVersion version of the previously active model (may be null if no previous model exists)
     * @return the persisted RetrainingReport entity
     */
    private RetrainingReport createReport(int itemCount, String message, boolean success, RetrainingResponse result, String previousVersion) {
        RetrainingReport report = new RetrainingReport();
        report.setTimestamp(LocalDateTime.now());
        report.setItemsProcessed(itemCount);
        report.setMessage(message);
        report.setSuccess(success);

        report.setPreviousVersion(previousVersion);

        // Add model version information if available
        if (result != null) {
            report.setModelVersion(result.getModelVersion());
            report.setAccuracy(result.getAccuracy());
            report.setPrecision(result.getPrecision());
            report.setRecall(result.getRecall());
            report.setTrainedSampleCount(result.getTrainedSampleCount());
            report.setActive(true); // Set as active model if successful
        }

        return retrainingReportRepository.save(report);
    }
}
