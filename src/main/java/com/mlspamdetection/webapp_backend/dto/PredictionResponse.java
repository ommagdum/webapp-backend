package com.mlspamdetection.webapp_backend.dto;

public class PredictionResponse {

    private int prediction;
    private double probability;
    private String errorMessage; // New field for error messages


    public PredictionResponse(int prediction, double probability, String errorMessage) {
        this.prediction = prediction;
        this.probability = probability;
        this.errorMessage = errorMessage;
    }


    // Getters and setters for all fields

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

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
