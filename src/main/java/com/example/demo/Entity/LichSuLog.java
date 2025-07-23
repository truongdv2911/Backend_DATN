package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table (name = "Lich_su_log")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LichSuLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "hanh_dong")
    private String hanhDong;

    private String bang;

    @Column(name = "mo_ta", columnDefinition = "nvarchar(max)")
    private String moTa;

    @Column(name = "thoi_gian")
    private LocalDateTime thoiGian;
    @Column(name = "user_id")
    private Integer userId;
}
