package com.example.demo.Controller;

import com.example.demo.Entity.SanPham;
import com.example.demo.Responses.ChatResponse;
import com.example.demo.Responses.ErrorResponse;
import com.example.demo.Service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/lego-store/chat")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @PostMapping
    public ResponseEntity<?> chat(@RequestBody String userInput) throws Exception{
        try {
            ChatResponse response = chatService.handleUserInput(userInput);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            ChatResponse errorResponse = new ChatResponse("ERROR",
                    "Đã có lỗi xảy ra. Vui lòng thử lại sau.",null);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(errorResponse);
        }
    }
}
