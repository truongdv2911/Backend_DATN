package com.example.demo.Responses;

import com.example.demo.Entity.SanPham;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HangThanhLyResponse {
    private SanPhamResponseDTO sanPhamResponseDTO;
    private Integer soLuong;
    private LocalDateTime ngayNhap;
    private String ghiChu;
}
