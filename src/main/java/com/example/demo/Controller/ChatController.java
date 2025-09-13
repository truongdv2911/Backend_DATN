package com.example.demo.Controller;

import com.example.demo.Entity.ChatRequest;
import com.example.demo.Responses.ChatResponse;
import com.example.demo.Service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/lego-store/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping("/start")
    public Map<String, String> startChatSession() {
        String sessionId = UUID.randomUUID().toString();
        return Map.of("sessionId", sessionId, "message", "Chat session started!");
    }

    @PostMapping("/message")
    public ResponseEntity<?> chat(@RequestBody ChatRequest userInput) throws Exception{
        try {
            return ResponseEntity.ok(chatService.handleUserInput(userInput.getMessage(), userInput.getSessionId()));
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse("ERROR",
                    "Đã có lỗi xảy ra. Vui lòng thử lại sau.",null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
    // Clear session
    @DeleteMapping("/session/{sessionId}")
    public Map<String, String> clearSession(@PathVariable String sessionId) {
        chatService.clearChatMemory(sessionId);
        return Map.of("message", "Session cleared successfully");
    }

    @GetMapping("/debug/database")
    public Map<String, String> debugDatabase() {
        String result = chatService.debugDatabaseData();
        return Map.of("debug", result);
    }

    @GetMapping("/debug/simple")
    public Map<String, String> testSimpleQuery() {
        String result = chatService.testSimpleQuery();
        return Map.of("test", result);
    }

    @GetMapping("/debug/search-flow")
    public Map<String, String> debugSearchFlow() {
        String result = chatService.debugSearchFlow();
        return Map.of("debug", result);
    }
    
    @GetMapping("/debug/parsing")
    public Map<String, String> debugParsing() {
        String result = chatService.testParsing();
        return Map.of("test", result);
    }
    
    @GetMapping("/debug/simple-api")
    public Map<String, String> debugSimpleAPI() {
        String result = chatService.testSimpleAPI();
        return Map.of("test", result);
    }
    
    @GetMapping("/debug/fix-12tuoi")
    public Map<String, String> testFix12Tuoi() {
        String result = chatService.testFix12Tuoi();
        return Map.of("test", result);
    }
    
    @GetMapping("/debug/fix-thailan")
    public Map<String, String> testFixThaiLan() {
        String result = chatService.testFixThaiLan();
        return Map.of("test", result);
    }
    
    @GetMapping("/debug/comprehensive-parsing")
    public Map<String, String> testComprehensiveParsing() {
        String result = chatService.testComprehensiveParsing();
        return Map.of("test", result);
    }

    @GetMapping("/session/{sessionId}/info")
    public Map<String, Object> getSessionInfo(@PathVariable String sessionId) {
        int memorySize = chatService.getChatMemorySize(sessionId);
        return Map.of(
                "sessionId", sessionId,
                "memorySize", memorySize,
                "maxMemorySize", 10
        );
    }
}
