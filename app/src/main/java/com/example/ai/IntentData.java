package com.example.ai;

import java.util.Map;

public class IntentData {

    private String intentType;
    private Map<String, String> slots;
    private double confidence;

    public IntentData(String intentType, Map<String, String> slots, double confidence) {
        this.intentType = intentType;
        this.slots = slots;
        this.confidence = confidence;
    }

    public String getIntentType() {
        return intentType;
    }

    public Map<String, String> getSlots() {
        return slots;
    }

    public double getConfidence() {
        return confidence;
    }
}