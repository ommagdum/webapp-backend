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

@Service
public class StatsService {

    private final PredictionLogRepository predictionLogRepository;
    private final FeedbackRepository feedbackRepository;

    @Autowired
    public StatsService(PredictionLogRepository predictionLogRepository, FeedbackRepository feedbackRepository) {
        this.predictionLogRepository = predictionLogRepository;
        this.feedbackRepository = feedbackRepository;
    }

    @Cacheable(value = "userStats", key = "#user.id", condition = "#user != null", unless = "#result == null")
    public StatsResponse getUserStats(User user){
        StatsResponse response = new StatsResponse();

        // Cal Total Checks
        long totalChecks = predictionLogRepository.countByUser(user);
        response.setTotalChecks(totalChecks);

        // Cal Spam Ratio
        long spamDetected = predictionLogRepository.countByUserAndIsSpam(user, true);
        long hamDetected = totalChecks - spamDetected;

        response.setSpamDetected(spamDetected);
        response.setHamDetected(hamDetected);

        if (totalChecks > 0) {
            response.setSpamRatio((double) spamDetected / totalChecks);
        } else {
            response.setSpamRatio(0.0);
        }

        // Cal Accuracy
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
