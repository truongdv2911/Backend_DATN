package com.example.demo.Service;

import com.example.demo.Entity.LichSuLog;
import com.example.demo.Entity.User;
import com.example.demo.Repository.LichSuLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class LichSuLogService {
    private final LichSuLogRepository lichSuLogRepository;

    public void saveLog(String hanhDong, String bang, String moTa, Integer userId) {
        LichSuLog log = new LichSuLog();
        log.setHanhDong(hanhDong);
        log.setBang(bang);
        log.setMoTa(moTa);
        log.setThoiGian(LocalDateTime.now());
        log.setUserId(userId);
        lichSuLogRepository.save(log);
    }
    public Integer getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()) {
            User userDetails = (User) authentication.getPrincipal();
            return userDetails.getId();
        }
        return null;
    }
}
