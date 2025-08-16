package com.example.demo.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class IntentClassificationDTO {
    private String intent; // SEARCH, ADVICE, SHIPPING, FAQ, GENERAL
    private String confidence;
    private String extractedInfo;

    // Getters and setters
    public String getIntent() { return intent; }
    public void setIntent(String intent) { this.intent = intent; }
    public String getConfidence() { return confidence; }
    public void setConfidence(String confidence) { this.confidence = confidence; }
    public String getExtractedInfo() { return extractedInfo; }
    public void setExtractedInfo(String extractedInfo) { this.extractedInfo = extractedInfo; }
}
