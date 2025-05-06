package com.mlspamdetection.webapp_backend.dto;

public class RetrainingResponse {
    private boolean success;
    private String message;
    private String modelVersion;
    private double accuracy;
    private double precision;
    private double recall;
    private int trainedSampleCount;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public void setModelVersion(String modelVersion) {
        this.modelVersion = modelVersion;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public double getPrecision() {
        return precision;
    }

    public void setPrecision(double precision) {
        this.precision = precision;
    }

    public double getRecall() {
        return recall;
    }

    public void setRecall(double recall) {
        this.recall = recall;
    }

    public int getTrainedSampleCount() {
        return trainedSampleCount;
    }

    public void setTrainedSampleCount(int trainedSampleCount) {
        this.trainedSampleCount = trainedSampleCount;
    }
}
