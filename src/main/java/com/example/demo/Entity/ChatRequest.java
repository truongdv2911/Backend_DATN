package com.example.demo.Entity;

import lombok.Data;

@Data
public class ChatRequest {
    private String message;
    private String sessionId;

    // constructors, getters, setters
    public ChatRequest() {}

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getSessionId() { return sessionId; }
    public void setSessionId(String sessionId) { this.sessionId = sessionId; }
}
