package com.mlspamdetection.webapp_backend.dto;

public class PredictionResponse {
    private int prediction;
    private double probability;

    public PredictionResponse(int prediction, double probability) {
        this.prediction = prediction;
        this.probability = probability;
    }

    public int getPrediction() {
        return prediction;
    }

    public void setPrediction(int prediction) {
        this.prediction = prediction;
    }

    public double getProbability() {
        return probability;
    }

    public void setProbability(double probability) {
        this.probability = probability;
    }
}
