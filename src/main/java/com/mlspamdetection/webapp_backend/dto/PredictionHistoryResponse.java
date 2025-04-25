package com.mlspamdetection.webapp_backend.dto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class PredictionHistoryResponse {
    private List<PredictionHistoryItem> content;
    private int page;
    private int totalPages;

    public PredictionHistoryResponse() {
    }

    public PredictionHistoryResponse(List<PredictionHistoryItem> content, int page, int totalPages) {
        this.content = content;
        this.page = page;
        this.totalPages = totalPages;
    }

    public List<PredictionHistoryItem> getContent() {
        return content;
    }

    public void setContent(List<PredictionHistoryItem> content) {
        this.content = content;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public static class PredictionHistoryItem {
        private Long id;
        private String contentSnippet;
        private boolean isSpam;
        private double confidence;
        private LocalDateTime timestamp;

        public PredictionHistoryItem(Long id, String contentSnippet, boolean isSpam, double confidence, LocalDateTime timestamp) {
            this.id = id;
            this.contentSnippet = contentSnippet;
            this.isSpam = isSpam;
            this.confidence = confidence;
            this.timestamp = timestamp;
        }



        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getContentSnippet() {
            return contentSnippet;
        }

        public void setContentSnippet(String contentSnippet) {
            this.contentSnippet = contentSnippet;
        }

        public boolean isSpam() {
            return isSpam;
        }

        public void setSpam(boolean spam) {
            isSpam = spam;
        }

        public double getConfidence() {
            return confidence;
        }

        public void setConfidence(double confidence) {
            this.confidence = confidence;
        }

        public LocalDateTime getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(LocalDateTime timestamp) {
            this.timestamp = timestamp;
        }
    }
}
