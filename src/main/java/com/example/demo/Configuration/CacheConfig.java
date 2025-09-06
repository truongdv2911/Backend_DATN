package com.example.demo.Configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.example.demo.Service.ChatService;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class CacheConfig {
    
    private final ChatService chatService;
    
    /**
     * Dọn dẹp cache mỗi 10 phút
     */
    @Scheduled(fixedRate = 600000) // 10 phút
    public void clearExpiredCache() {
        chatService.clearExpiredCache();
    }
}
