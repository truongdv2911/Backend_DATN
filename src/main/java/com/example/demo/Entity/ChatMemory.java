package com.example.demo.Entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
public class ChatMemory {
    private String userInput;
    private String botResponse;
    private String intent;
    private long timestamp;

    public ChatMemory(String userInput, String botResponse, String intent) {
        this.userInput = userInput;
        this.botResponse = botResponse;
        this.intent = intent;
        this.timestamp = System.currentTimeMillis();
    }

    // Getters and setters
    public String getUserInput() { return userInput; }
    public String getBotResponse() { return botResponse; }
    public String getIntent() { return intent; }
    public long getTimestamp() { return timestamp; }
}
