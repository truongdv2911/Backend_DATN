package com.example.demo.Responses;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ChiTietKMResponse {
    private Integer id;
    private String tenKhuyenMai;
    private Integer phanTramKhuyenMai;
    private LocalDateTime ngayBatDau;
    private LocalDateTime ngayKetThuc;
    private Integer soSanPhamApDung;
    private Integer tongSoLuongBan;
    private BigDecimal tongTienTruocGiam;
    private BigDecimal tongSoTienGiam;
    private BigDecimal tongTienSauGiam;
    private Integer soHoaDon;
    private List<Object> sanPhamDaApDung;
}
