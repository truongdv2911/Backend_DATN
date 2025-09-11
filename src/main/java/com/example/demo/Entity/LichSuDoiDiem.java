package com.example.demo.Entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@Entity
@Table(name = "lich_su_doi_diem")
public class LichSuDoiDiem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "phieu_giam_gia_id")
    private PhieuGiamGia phieuGiamGia;

    @Column(name = "diem_da_doi")
    private Integer diemDaDoi;

    @Column(nullable = false, columnDefinition = "DATETIME DEFAULT GETDATE()", name = "ngay_doi")
    private LocalDateTime ngayDoi = LocalDateTime.now();
    @Column(name = "mo_ta")
    private String moTa;
}