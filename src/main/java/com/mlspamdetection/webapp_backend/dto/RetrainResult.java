package com.mlspamdetection.webapp_backend.dto;

public class RetrainResult {
    private String newVersion;
    private double accuracy;
    private boolean rollbackPerformed;

    

    public RetrainResult(String newVersion, double accuracy, boolean rollbackPerformed) {
        this.newVersion = newVersion;
        this.accuracy = accuracy;
        this.rollbackPerformed = rollbackPerformed;
    }
    public String getNewVersion() {
        return newVersion;
    }
    public void setNewVersion(String newVersion) {
        this.newVersion = newVersion;
    }
    public double getAccuracy() {
        return accuracy;
    }
    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }
    public boolean isRollbackPerformed() {
        return rollbackPerformed;
    }
    public void setRollbackPerformed(boolean rollbackPerformed) {
        this.rollbackPerformed = rollbackPerformed;
    }

    
}
