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

@Service
public class RetrainingService {

    private static final Logger logger = LoggerFactory.getLogger(RetrainingService.class);

    private final FeedbackRepository feedbackRepository;
    private final PredictionLogRepository predictionLogRepository;
    private final RetrainingReportRepository retrainingReportRepository;
    private final RestTemplate restTemplate;

    @Value("${ml.service.url}")
    private String mlServiceUrl;

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
