package com.mlspamdetection.webapp_backend.service;

import com.mlspamdetection.webapp_backend.dto.StatsResponse;
import com.mlspamdetection.webapp_backend.model.User;
import com.mlspamdetection.webapp_backend.repo.FeedbackRepository;
import com.mlspamdetection.webapp_backend.repo.PredictionLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service responsible for generating user-specific statistics and metrics.
 * 
 * <p>This service provides functionality for calculating and retrieving various
 * statistics related to a user's spam detection activity, including:</p>
 * <ul>
 *   <li>Total number of emails checked</li>
 *   <li>Spam to ham ratio</li>
 *   <li>Prediction accuracy based on user feedback</li>
 *   <li>Daily usage trends over the past week</li>
 * </ul>
 * 
 * <p>The service uses caching to improve performance for frequently accessed user statistics.</p>
 */
@Service
public class StatsService {

    /**
     * Repository for accessing prediction log data.
     */
    private final PredictionLogRepository predictionLogRepository;
    
    /**
     * Repository for accessing feedback data.
     */
    private final FeedbackRepository feedbackRepository;

    /**
     * Constructs a StatsService with the necessary dependencies.
     *
     * @param predictionLogRepository repository for prediction log data access
     * @param feedbackRepository repository for feedback data access
     */
    @Autowired
    public StatsService(PredictionLogRepository predictionLogRepository, FeedbackRepository feedbackRepository) {
        this.predictionLogRepository = predictionLogRepository;
        this.feedbackRepository = feedbackRepository;
    }

    /**
     * Retrieves comprehensive statistics for a specific user.
     * 
     * <p>This method calculates various metrics based on the user's prediction history and feedback:</p>
     * <ol>
     *   <li>Total number of emails checked by the user</li>
     *   <li>Number of spam and ham emails detected</li>
     *   <li>Spam ratio (percentage of emails classified as spam)</li>
     *   <li>Prediction accuracy based on user feedback</li>
     *   <li>Daily usage trends for the past 7 days</li>
     * </ol>
     * 
     * <p>The results are cached to improve performance for repeated requests. The cache is
     * keyed by the user's ID and will only cache results for non-null users and responses.</p>
     *
     * @param user the user for whom to retrieve statistics
     * @return a StatsResponse object containing the calculated statistics
     */
    @Cacheable(value = "userStats", key = "#user.id", condition = "#user != null", unless = "#result == null")
    public StatsResponse getUserStats(User user){
        StatsResponse response = new StatsResponse();

        // Calculate Total Checks
        long totalChecks = predictionLogRepository.countByUser(user);
        response.setTotalChecks(totalChecks);

        // Calculate Spam Ratio
        long spamDetected = predictionLogRepository.countByUserAndIsSpam(user, true);
        long hamDetected = totalChecks - spamDetected;

        response.setSpamDetected(spamDetected);
        response.setHamDetected(hamDetected);

        if (totalChecks > 0) {
            response.setSpamRatio((double) spamDetected / totalChecks);
        } else {
            response.setSpamRatio(0.0);
        }

        // Calculate Accuracy
        long processedFeedback = feedbackRepository.countByUserAndProcessed(user, true);
        if (processedFeedback > 0) {
            long correctPredictions = feedbackRepository.countCorrectPredictions(user);
            response.setAccuracy((double) correctPredictions / processedFeedback);
        } else {
            response.setAccuracy(0.0);
        }

        // Get Daily counts for last 7 days
        LocalDateTime startDate = LocalDate.now().minusDays(6).atStartOfDay();
        List<Object[]> dailyCounts = predictionLogRepository.getDailyCounts(user, startDate);

        List<StatsResponse.DailyCount> dailyCountList = new ArrayList<>();

        // Create a map of date to count from query results
        Map<LocalDate, Long> countsMap = dailyCounts.stream()
                .collect(Collectors.toMap(
                        row -> ((java.sql.Date) row[0]).toLocalDate(),
                        row -> (Long) row[1]
                ));

        // Format and populate daily counts for the last 7 days
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        for (int i = 0; i < 7; i++) {
            LocalDate date = LocalDate.now().minusDays(6 - i);
            long count = countsMap.getOrDefault(date, 0L);
            dailyCountList.add(new StatsResponse.DailyCount(date.format(formatter), count));
        }

        response.setDailyCounts(dailyCountList);

        return response;
    }
}
