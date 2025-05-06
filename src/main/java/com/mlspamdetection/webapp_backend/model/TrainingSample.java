package com.mlspamdetection.webapp_backend.model;

public class TrainingSample {
    private String text;
    private int label;
    
    public TrainingSample() {}
    
    public TrainingSample(String text, int label) {
        this.text = text;
        this.label = label;
    }
    
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    
    public int getLabel() { return label; }
    public void setLabel(int label) { this.label = label; }
}
