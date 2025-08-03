package com.example.demo.Controller;

import com.example.demo.Entity.SanPham;
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
            List<SanPham> result = chatService.handleUserInput(userInput);
            return ResponseEntity.ok(result);
        }catch (Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new ErrorResponse(500, e.getMessage()));
        }
    }
}
