package com.mlspamdetection.webapp_backend.dto;

import java.util.List;

public class StatsResponse {
    private long totalChecks;
    private double spamRatio;
    private double accuracy;
    private long spamDetected; // Rename from spamCount
    private long hamDetected;  // Rename from hamCount


    public long getSpamDetected() {
        return spamDetected;
    }

    public void setSpamDetected(long spamDetected) {
        this.spamDetected = spamDetected;
    }

    public long getHamDetected() {
        return hamDetected;
    }

    public void setHamDetected(long hamDetected) {
        this.hamDetected = hamDetected;
    }

    private List<DailyCount> dailyCounts;

    public StatsResponse() {
    }

    public long getTotalChecks() {
        return totalChecks;
    }

    public void setTotalChecks(long totalChecks) {
        this.totalChecks = totalChecks;
    }

    public double getSpamRatio() {
        return spamRatio;
    }

    public void setSpamRatio(double spamRatio) {
        this.spamRatio = spamRatio;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public List<DailyCount> getDailyCounts() {
        return dailyCounts;
    }

    public void setDailyCounts(List<DailyCount> dailyCounts) {
        this.dailyCounts = dailyCounts;
    }

    public static class DailyCount {
        private String date;
        private long count;

        public DailyCount() {
        }

        public DailyCount(String date, long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }

        public long getCount() {
            return count;
        }

        public void setCount(long count) {
            this.count = count;
        }
    }
}
