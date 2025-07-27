package com.example.demo.Responses;

import com.example.demo.Entity.LichSuLog;
import com.example.demo.Entity.User;
import com.example.demo.Repository.UserRepository;
import jakarta.persistence.Column;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

@Data
public class LogResponse {

    private Integer id;

    private String hanhDong;

    private String bang;

    private String moTa;

    private LocalDateTime thoiGian;

    private User user;

    public LogResponse(LichSuLog log, User user) {
        this.id = log.getId();
        this.hanhDong = log.getHanhDong();
        this.bang = log.getBang();
        this.moTa = log.getMoTa();
        this.thoiGian = log.getThoiGian();
        this.user = user;
    }
}
