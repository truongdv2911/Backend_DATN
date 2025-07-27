package com.example.demo.Controller;

import com.example.demo.Entity.LichSuLog;
import com.example.demo.Entity.User;
import com.example.demo.Repository.LichSuLogRepository;
import com.example.demo.Repository.UserRepository;
import com.example.demo.Responses.LogResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/lego-store/lich-su-log")
@RequiredArgsConstructor
public class LichSuLogController {
    private final LichSuLogRepository lichSuLogRepository;
    private final UserRepository userRepository;

    // Xem toàn bộ log, có phân trang
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllLogs() {
        List<LichSuLog> logs = lichSuLogRepository.findAll();
        List<LogResponse> response = logs.stream().map(log -> {
            User user = null;
            if (log.getUserId() != null) {
                user = userRepository.findById(log.getUserId()).orElse(null);
            }
            return new LogResponse(log, user);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    // Xem log theo tên bảng (entity), có phân trang
    @GetMapping("/by-bang")
    public ResponseEntity<?> getLogsByBang(@RequestParam String bang) {
        List<LichSuLog> logs = lichSuLogRepository.findByBangIgnoreCase(bang);
        List<LogResponse> response = logs.stream().map(log -> {
            User user = null;
            if (log.getUserId() != null) {
                user = userRepository.findById(log.getUserId()).orElse(null);
            }
            return new LogResponse(log, user);
        }).collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }
} 