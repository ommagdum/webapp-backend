package com.mlspamdetection.webapp_backend.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public class FeedbackRequest {

    @NotNull(message = "Prediction ID must be provided")
    @Min(value = 1, message = "Invalid prediction ID")
    @JsonProperty("prediction_id")
    private Long predictionId; // camelCase field name

    @NotBlank(message = "Corrected label is required")
    @Pattern(regexp = "^(spam|ham)$", 
             message = "Label must be exactly 'spam' or 'ham' in lowercase")
    @JsonProperty("corrected_label")
    private String correctedLabel; // camelCase field name

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
