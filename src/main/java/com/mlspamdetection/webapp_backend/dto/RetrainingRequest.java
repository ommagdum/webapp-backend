package com.mlspamdetection.webapp_backend.dto;

import java.util.List;

public class RetrainingRequest {
    private List<TrainingItem> trainingData;

    public List<TrainingItem> getTrainingData() {
        return trainingData;
    }

    public void setTrainingData(List<TrainingItem> trainingData){
        this.trainingData = trainingData;
    }

    public static class TrainingItem{
        private String content;
        private String label;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }
}
