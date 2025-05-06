package com.mlspamdetection.webapp_backend.scheduler;

import com.mlspamdetection.webapp_backend.service.RetrainingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class RetrainingScheduler {

    private static final Logger logger = LoggerFactory.getLogger(RetrainingScheduler.class);
    private final RetrainingService retrainingService;

    @Autowired
    public RetrainingScheduler(RetrainingService retrainingService){
        this.retrainingService = retrainingService;
    }

    // Run daily at midnight UTC (cron = second, minute, hour, day, month, weekday)

    @Scheduled(cron = "0 0 0 * * *")
    public void scheduledRetraining() {
        logger.info("Starting scheduled retraining...");
        retrainingService.processUnprocessedFeedback();
    }
}
