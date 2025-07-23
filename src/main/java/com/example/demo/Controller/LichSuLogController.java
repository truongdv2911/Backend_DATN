package com.example.demo.Controller;

import com.example.demo.Entity.LichSuLog;
import com.example.demo.Repository.LichSuLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import java.util.List;

@RestController
@RequestMapping("/api/lego-store/lich-su-log")
@RequiredArgsConstructor
public class LichSuLogController {
    private final LichSuLogRepository lichSuLogRepository;

    // Xem toàn bộ log, có phân trang
    @GetMapping("/getAll")
    public ResponseEntity<?> getAllLogs() {
        List<LichSuLog> logs = lichSuLogRepository.findAll();
        return ResponseEntity.ok(logs);
    }

    // Xem log theo tên bảng (entity), có phân trang
    @GetMapping("/by-bang")
    public ResponseEntity<?> getLogsByBang(@RequestParam String bang) {
        List<LichSuLog> logs = lichSuLogRepository.findByBangIgnoreCase(bang);
        return ResponseEntity.ok(logs);
    }
} 