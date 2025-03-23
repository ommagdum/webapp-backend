package com.mlspamdetection.webapp_backend.dto;

public class PredictionResult {
    private String prediction;
    private Double probability;

    public PredictionResult(String prediction, double probability) {
        this.prediction = prediction;
        this.probability = probability;
    }

    public String getPrediction() {
        return prediction;
    }

    public void setPrediction(String prediction) {
        this.prediction = prediction;
    }

    public Double getProbability() {
        return probability;
    }

    public void setProbability(Double probability) {
        this.probability = probability;
    }
}
