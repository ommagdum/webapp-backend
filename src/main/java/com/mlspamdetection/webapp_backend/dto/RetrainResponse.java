package com.mlspamdetection.webapp_backend.dto;

public class RetrainResponse {
    private String newVersion;
    private double accuracy;
    private String modelSize;
    
    public RetrainResponse() {}
    
    public RetrainResponse(String newVersion, double accuracy, String modelSize) {
        this.newVersion = newVersion;
        this.accuracy = accuracy;
        this.modelSize = modelSize;
    }
    
    public String getNewVersion() { return newVersion; }
    public void setNewVersion(String newVersion) { this.newVersion = newVersion; }
    
    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }
    
    public String getModelSize() { return modelSize; }
    public void setModelSize(String modelSize) { this.modelSize = modelSize; }
}
