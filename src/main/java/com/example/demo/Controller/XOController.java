package com.example.demo.Controller;

import com.example.demo.Component.PlayerSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/lego-store/xo")
public class XOController {
    private final Map<Integer, PlayerSession> sessions = new ConcurrentHashMap<>();

    @PostMapping("/result")
    public ResponseEntity<String> recordResult(
            @RequestParam Integer userId,
            @RequestParam String result) {

        PlayerSession session = sessions.computeIfAbsent(userId, id -> new PlayerSession());
        session.recordOutcome(result);

        return ResponseEntity.ok("New difficulty: " + session.getCurrentDifficulty());
    }

    @GetMapping("/difficulty")
    public ResponseEntity<String> getDifficulty(@RequestParam Integer userId) {
        PlayerSession session = sessions.computeIfAbsent(userId, id -> new PlayerSession());
        return ResponseEntity.ok(session.getCurrentDifficulty().name());
    }
}
