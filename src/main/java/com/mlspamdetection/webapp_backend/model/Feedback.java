package com.mlspamdetection.webapp_backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "feedback")
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private PredictionLog prediction;

    @Column(name = "corrected_label")
    private String correctedLabel;

    @Column
    private LocalDateTime timestamp;

    @Column
    private boolean processed = false;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Default constructor
    public Feedback() {
        this.timestamp = LocalDateTime.now();
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public PredictionLog getPrediction() {
        return prediction;
    }

    public void setPrediction(PredictionLog prediction) {
        this.prediction = prediction;
    }

    public String getCorrectedLabel() {
        return correctedLabel;
    }

    public void setCorrectedLabel(String correctedLabel) {
        this.correctedLabel = correctedLabel;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isProcessed() {
        return processed;
    }

    public void setProcessed(boolean processed) {
        this.processed = processed;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
