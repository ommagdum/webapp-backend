package com.mlspamdetection.webapp_backend.dto;

import java.util.UUID;

public class FeedbackRequest {
    private Long predictionId;
    private String correctedLabel;

    public FeedbackRequest() {
    }

    public FeedbackRequest(Long predictionId, String correctedLabel) {
        this.predictionId = predictionId;
        this.correctedLabel = correctedLabel;
    }

    public Long getPredictionId() {
        return predictionId;
    }

    public void setPredictionId(Long predictionId) {
        this.predictionId = predictionId;
    }

    public String getCorrectedLabel() {
        return correctedLabel;
    }

    public void setCorrectedLabel(String correctedLabel) {
        this.correctedLabel = correctedLabel;
    }
}
