package com.example.demo.Controller;

import com.example.demo.Entity.ChatRequest;
import com.example.demo.Responses.ChatResponse;
import com.example.demo.Service.ChatService2;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class Chat2Controller {
    private final ChatService2 chatService;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody ChatRequest request) {
        return ResponseEntity.ok(chatService.handlePrompt(request.getMessage()));
    }
}
