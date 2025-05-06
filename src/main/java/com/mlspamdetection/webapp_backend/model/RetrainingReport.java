package com.mlspamdetection.webapp_backend.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "retraining_reports")
public class RetrainingReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = false)
    private int itemsProcessed;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Column(nullable = false)
    private boolean success;

    @Column(name = "model_version")
    private String modelVersion;

    @Column(name = "previous_version")
    private String previousVersion;

    public String getPreviousVersion() {
        return previousVersion;
    }

    public void setPreviousVersion(String previousVersion) {
        this.previousVersion = previousVersion;
    }

    @Column
    private double accuracy;

    @Column
    private double precision;

    @Column
    private double recall;

    @Column(name = "training_sample_count")
    private int trainedSampleCount;

    @Column(name = "is_active")
    private boolean isActive;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public int getItemsProcessed() {
        return itemsProcessed;
    }

    public void setItemsProcessed(int itemsProcessed) {
        this.itemsProcessed = itemsProcessed;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }
}
